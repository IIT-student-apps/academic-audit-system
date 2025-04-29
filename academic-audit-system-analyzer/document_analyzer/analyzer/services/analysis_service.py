import json
import traceback
from typing import Optional

from analyzer.utils.analyzers import TextAnalyzer
from analyzer.utils.plagiarism import PlagiarismService
from analyzer.utils.processors import DocumentProcessor


class AnalysisService:
    @staticmethod
    def analyze_document(file_content: bytes, original_filename: Optional[str] = None) -> str:
        """
        Анализирует документ и возвращает отчет в формате JSON.
        Включает текстовый анализ и проверку на плагиат.

        Args:
            file_content: содержимое файла (bytes)
            original_filename: название файла (опционально)

        Returns:
            str: JSON строка с результатами анализа
        """
        try:
            # Определяем имя файла и расширение
            filename, file_ext = DocumentProcessor.get_file_info(file_content, original_filename)

            # Извлекаем текст из документа
            text = DocumentProcessor.extract_text_from_bytes(file_content, file_ext)

            # Выполняем полный анализ текста
            analyzer = TextAnalyzer()
            text_analysis = analyzer.full_analysis(text)

            # Проверяем на плагиат
            plagiarism_results = PlagiarismService.check_all_services(text, 300)

            # Формируем итоговый отчет
            report = {
                "status": "COMPLETED",
                "text_analysis": text_analysis,
                "plagiarism_check": plagiarism_results,
            }

            return json.dumps(report, indent=4, ensure_ascii=False)

        except Exception as e:
            error_report = {
                "status": "FAILED",
                "error": str(e),
                "stack_trace": traceback.format_exc(),
            }
            return json.dumps(error_report, ensure_ascii=False)