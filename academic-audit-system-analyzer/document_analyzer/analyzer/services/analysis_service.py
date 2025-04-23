import json
import os
import tempfile
import traceback

from analyzer.utils.analyzers import TextAnalyzer
from analyzer.utils.plagiarism import PlagiarismService
from analyzer.utils.processors import DocumentProcessor


class AnalysisService:
	@staticmethod
	def analyze_document(file_content, original_filename=None):
		"""
		Анализирует документ и возвращает отчет в формате JSON
		Включает текстовый анализ и проверку на плагиат

		Args:
			file_content: bytes - содержимое файла из GridFS
			original_filename: None - название файла
		Returns:
			str: JSON строка с результатами анализа
		"""
		try:
			with tempfile.NamedTemporaryFile(
					delete=False,
					suffix=os.path.splitext(original_filename)[1] if original_filename else '.txt'
			) as temp_file:
				temp_file.write(file_content)
				temp_file_path = temp_file.name

			# Определяем тип файла по расширению
			file_ext = DocumentProcessor.get_file_extension(temp_file_path)

			# Извлекаем текст из документа
			text = DocumentProcessor.extract_text(temp_file_path, file_ext)

			# Удаляем временный файл
			os.unlink(temp_file_path)

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
					"content_length": len(text)
				}
			}

			return json.dumps(report)

		except Exception as e:
			error_report = {
				"status": "FAILED",
				"error": str(e),
				"stack_trace": traceback.format_exc()
			}
			return json.dumps(error_report)
