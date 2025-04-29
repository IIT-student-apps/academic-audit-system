import json
from analyzer.services.analysis_service import AnalysisService


def test_analyze_document():
    # 1. Читаем тестовый файл как бинарные данные (как будто из GridFS)
    with open("test_document.txt", "rb") as f:
        file_content = f.read()

    # 2. Запускаем анализ
    result_json = AnalysisService.analyze_document(file_content)

    # 3. Выводим результат в читаемом виде
    result = json.loads(result_json)
    pretty_json = json.dumps(result, indent=4, ensure_ascii=False)  # ensure_ascii=False для кириллицы

    print(pretty_json)


if __name__ == "__main__":
    test_analyze_document()
