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
    def check_all_services(text, title=None, max_wait_time=300):
        """Проверяет текст и ждёт результат не дольше max_wait_time секунд."""
        return {
            "text_ru": PlagiarismService._check_textru(text, title, max_wait_time),
        }

    @staticmethod
    def _check_textru(text, title=None, max_wait_time=300):
        """Отправляет текст и ждёт результат с прогрессивной проверкой."""
        try:
            # 1. Отправка текста и получение UID
            uid = PlagiarismService._submit_text(text, title)
            if not uid:
                raise APIException("Не удалось получить UID проверки")

            # 2. Ожидание результата с прогрессивным интервалом
            return PlagiarismService._wait_for_result(uid, max_wait_time)

        except Exception as e:
            raise APIException(f"Ошибка Text.ru: {str(e)}")

    @staticmethod
    def _submit_text(text, title):
        """Отправляет текст на проверку и возвращает UID."""
        response = requests.post(
            'https://api.text.ru/post',
            data={
                'text': text,
                'userkey': PlagiarismService.TEXT_RU_API_KEY,
                'title': title or '',
                'visible': 'vis_on',
                'copying': 'noadd'
            },
        )
        response.raise_for_status()
        data = response.json()

        if 'error_code' in data:
            raise APIException(f"Text.ru API error: {data.get('error_desc', 'Unknown error')}")

        return data.get('text_uid') or data.get('uid')

    @staticmethod
    def _wait_for_result(uid, max_wait_time):
        """Ожидает результат с увеличивающимся интервалом проверки."""
        start_time = time.time()
        check_interval = 5  # Начинаем с 5 секунд

        last_status = None

        while time.time() - start_time < max_wait_time:
            result = PlagiarismService._get_result(uid)

            if result['status'] == 'completed':
                return {
                    'unique': float(result.get('text_unique', 0)),
                    'plagiarism': float(result.get('text_plagiat', 0)),
                    'water': float(result.get('water_percent', 0)),
                    'spam': float(result.get('spam_percent', 0)),
                    'seo_data': result.get('seo_check', {}),
                    'matches': result.get('matches', []),
                }
            elif result['status'] == 'error':
                raise APIException(result.get('message', 'Unknown error during check'))

            # Увеличиваем интервал проверки (но не более 30 секунд)
            check_interval = min(check_interval * 1.5, 30)

            # Логирование статуса (можно убрать в продакшене)
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

            # Обработка ошибок API
            if 'error_code' in data:
                error_desc = data.get('error_desc', '')
                if "ещё не проверен" in error_desc or "проверяется" in error_desc:
                    return {'status': 'pending'}
                return {
                    'status': 'error',
                    'message': error_desc or 'Unknown API error'
                }

            # Проверка завершена
            if 'text_unique' in data:
                # Обработка SEO данных (могут быть строкой JSON)
                seo_data = data.get('seo_check')
                if isinstance(seo_data, str):
                    try:
                        seo_data = json.loads(seo_data)
                    except json.JSONDecodeError:
                        seo_data = {}

                # Добавлена обработка списка совпадений (matches)
                matches = data.get('matches', [])
                if isinstance(matches, str):
                    try:
                        matches = json.loads(matches)
                    except json.JSONDecodeError:
                        matches = []

                return {
                    'status': 'completed',
                    'text_unique': data['text_unique'],
                    'text_plagiat': data.get('text_plagiat', '0'),
                    'water_percent': data.get('water_percent', '0'),
                    'spam_percent': data.get('spam_percent', '0'),
                    'seo_check': seo_data,
                    'matches': matches
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
    def async_check_all(text, title=None, max_wait_time=300):
        """Асинхронная проверка с ограниченным временем ожидания."""
        with ThreadPoolExecutor() as executor:
            future = executor.submit(
                PlagiarismService.check_all_services,
                text, title, max_wait_time
            )
            return future.result()