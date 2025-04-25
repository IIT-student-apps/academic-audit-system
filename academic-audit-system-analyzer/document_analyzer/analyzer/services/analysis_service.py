import json
import os
import tempfile
import traceback
from typing import Union
from io import BytesIO
from gridfs import GridOut

from analyzer.utils.analyzers import TextAnalyzer
from analyzer.utils.plagiarism import PlagiarismService
from analyzer.utils.processors import DocumentProcessor


class AnalysisService:
	@staticmethod
	def analyze_document(file_obj: Union[bytes, GridOut], original_filename: str = None) -> str:
		"""
		Анализирует документ и возвращает отчет в формате JSON.
		Включает текстовый анализ и проверку на плагиат.

		Args:
			file_obj: содержимое файла (bytes или объект GridOut из GridFS)
			original_filename: название файла (опционально)

		Returns:
			str: JSON строка с результатами анализа
		"""
		try:
			# Определяем расширение файла
			file_ext = None
			if original_filename:
				file_ext = DocumentProcessor.get_file_extension(original_filename)

			# Обрабатываем разные типы file_obj
			if isinstance(file_obj, GridOut):
				# Если это объект GridFS
				if original_filename is None:
					original_filename = getattr(file_obj, 'filename', 'document')
				file_content = file_obj.read()
			else:
				# Если это bytes
				file_content = file_obj

			# Создаем временный файл с правильным расширением
			suffix = file_ext if file_ext else ('.txt' if original_filename is None
												else os.path.splitext(original_filename)[1])

			with tempfile.NamedTemporaryFile(
					delete=False,
					suffix=suffix
			) as temp_file:
				temp_file.write(file_content)
				temp_file_path = temp_file.name

			# Если расширение не было определено ранее, определяем его по временному файлу
			if file_ext is None:
				file_ext = DocumentProcessor.get_file_extension(temp_file_path)

			# Извлекаем текст из документа
			text = DocumentProcessor.extract_text(temp_file_path, file_ext)

			# Удаляем временный файл
			try:
				os.unlink(temp_file_path)
			except Exception as e:
				print(f"Warning: Failed to delete temp file: {e}")

			# Выполняем полный анализ текста
			analyzer = TextAnalyzer()
			text_analysis = analyzer.full_analysis(text)

			# Проверяем на плагиат
			plagiarism_results = PlagiarismService.check_all_services(text, "analyzed_document")

			# Формируем итоговый отчет
			report = {
				"status": "COMPLETED",
				"text_analysis": text_analysis,
				"plagiarism_check": plagiarism_results,
				"metadata": {
					"file_type": file_ext,
					"content_length": len(text),
					"original_filename": original_filename
				}
			}

			return json.dumps(report, indent=4, ensure_ascii=False)

		except Exception as e:
			error_report = {
				"status": "FAILED",
				"error": str(e),
				"stack_trace": traceback.format_exc(),
				"metadata": {
					"file_type": file_ext if 'file_ext' in locals() else None,
					"original_filename": original_filename
				}
			}
			return json.dumps(error_report, ensure_ascii=False)
