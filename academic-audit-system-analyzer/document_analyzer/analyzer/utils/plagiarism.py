import json
import requests
import time
from concurrent.futures import ThreadPoolExecutor
from dotenv import load_dotenv
import os


class APIException(Exception):
    pass


load_dotenv()


class PlagiarismService:
    TEXT_RU_API_KEY = os.getenv('TEXT_RU_API_KEY')

    @staticmethod
    def check_all_services(text, max_wait_time=300):
        """Проверяет текст и ждёт результат не дольше max_wait_time секунд."""
        return {
            "text_ru": PlagiarismService._check_textru(text, max_wait_time),
        }

    @staticmethod
    def _check_textru(text, max_wait_time=300):
        """Отправляет текст и ждёт результат с прогрессивной проверкой."""
        try:
            # 1. Отправка текста и получение UID
            uid = PlagiarismService._submit_text(text)
            if not uid:
                raise APIException("Не удалось получить UID проверки")

            # 2. Ожидание результата с прогрессивным интервалом
            return PlagiarismService._wait_for_result(uid, max_wait_time)

        except Exception as e:
            raise APIException(f"Ошибка Text.ru: {str(e)}")

    @staticmethod
    def _submit_text(text):
        """Отправляет текст на проверку и возвращает UID."""
        response = requests.post(
            'https://api.text.ru/post',
            data={
                'text': text,
                'userkey': PlagiarismService.TEXT_RU_API_KEY,
            },
        )
        response.raise_for_status()
        data = response.json()

        if 'error_code' in data:
            raise APIException(f"Text.ru API error: {data.get('error_desc', 'Unknown error')}")

        return data.get('text_uid')

    @staticmethod
    def _wait_for_result(uid, max_wait_time):
        """Ожидает результат с увеличивающимся интервалом проверки."""
        start_time = time.time()
        check_interval = 5  # Начинаем с 5 секунд

        last_status = None

        while time.time() - start_time < max_wait_time:
            result = PlagiarismService._get_result(uid)

            if result['status'] == 'completed':
                # Обработка SEO данных
                seo_data = {}
                if 'seo_check' in result['data']:
                    if isinstance(result['data']['seo_check'], str):
                        try:
                            seo_data = json.loads(result['data']['seo_check'])
                        except json.JSONDecodeError:
                            seo_data = {}
                    else:
                        seo_data = result['data']['seo_check'] or {}

                # Обработка списка совпадений
                matches = []
                if 'result_json' in result['data'] and result['data']['result_json']:
                    try:
                        result_json = json.loads(result['data']['result_json']) if isinstance(result['data']['result_json'], str) else result['data']['result_json']
                        matches = result_json.get('urls', [])
                    except (json.JSONDecodeError, AttributeError):
                        matches = []

                # Обработка проверки орфографии
                spell_errors = []
                if 'spell_check' in result['data'] and result['data']['spell_check']:
                    try:
                        spell_data = json.loads(result['data']['spell_check']) if isinstance(result['data']['spell_check'], str) else result['data']['spell_check']
                        if isinstance(spell_data, list):
                            spell_errors = spell_data
                    except (json.JSONDecodeError, AttributeError):
                        spell_errors = []

                # Безопасное преобразование числовых значений
                def safe_float(value):
                    try:
                        return float(value) if value not in [None, ''] else 0.0
                    except (ValueError, TypeError):
                        return 0.0

                text_unique = safe_float(result['data'].get('text_unique'))
                water_percent = safe_float(seo_data.get('water_percent'))
                spam_percent = safe_float(seo_data.get('spam_percent'))

                return {
                    'plagiarism': 100 - text_unique,
                    'water': water_percent,
                    'spam': spam_percent,
                    'seo_data': {
                        'count_chars_with_space': seo_data.get('count_chars_with_space'),
                        'count_chars_without_space': seo_data.get('count_chars_without_space'),
                        'mixed_words': seo_data.get('mixed_words', []),
                    },
                    'matches': matches,
                    'spell_check': spell_errors
                }
            elif result['status'] == 'error':
                raise APIException(result.get('message', 'Unknown error during check'))

            check_interval = min(check_interval * 1.5, 30)
            if last_status != result['status']:
                print(f"Text.ru check status: {result['status']}, next check in {check_interval:.1f}s")
                last_status = result['status']

            time.sleep(check_interval)

        raise APIException(f"Проверка не завершена за {max_wait_time} секунд")

    @staticmethod
    def _get_result(uid):
        """Запрашивает текущий статус проверки."""
        try:
            response = requests.post(
                'https://api.text.ru/post',
                data={
                    'uid': uid,
                    'userkey': PlagiarismService.TEXT_RU_API_KEY,
                    'jsonvisible': 'detail'
                },
                timeout=10
            )
            response.raise_for_status()
            data = response.json()

            if 'error_code' in data:
                error_desc = data.get('error_desc', '')
                if "ещё не проверен" in error_desc or "проверяется" in error_desc:
                    return {'status': 'pending'}
                return {
                    'status': 'error',
                    'message': error_desc or 'Unknown API error'
                }

            if 'text_unique' in data:
                return {
                    'status': 'completed',
                    'data': data
                }

            return {'status': 'pending'}

        except requests.RequestException as e:
            return {
                'status': 'error',
                'message': f"Request failed: {str(e)}"
            }
        except json.JSONDecodeError:
            return {
                'status': 'error',
                'message': "Invalid JSON response from API"
            }

    @staticmethod
    def async_check_all(text, max_wait_time=300):
        """Асинхронная проверка с ограниченным временем ожидания."""
        with ThreadPoolExecutor() as executor:
            future = executor.submit(
                PlagiarismService.check_all_services,
                text, max_wait_time
            )
            return future.result()
