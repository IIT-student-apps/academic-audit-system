import json
import traceback

from document_analyzer.analyzer.utils.analyzers import TextAnalyzer
from document_analyzer.analyzer.utils.plagiarism import PlagiarismService
from document_analyzer.analyzer.utils.processors import DocumentProcessor


class AnalysisService:
	@staticmethod
	def analyze_document(file_content):
		"""
		Анализирует документ и возвращает отчет в формате JSON
		Включает текстовый анализ и проверку на плагиат

		Args:
			file_content: bytes - содержимое файла из GridFS

		Returns:
			str: JSON строка с результатами анализа
		"""
		try:
			# Сохраняем временный файл для обработки
			temp_file_path = "/tmp/temp_document"
			with open(temp_file_path, "wb") as f:
				f.write(file_content)

			# Определяем тип файла по расширению
			file_ext = DocumentProcessor.get_file_extension(temp_file_path)

			# Извлекаем текст из документа
			text = DocumentProcessor.extract_text(temp_file_path, file_ext)

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
