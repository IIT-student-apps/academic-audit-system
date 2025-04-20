import os

import requests
import time
from bs4 import BeautifulSoup
import json
from concurrent.futures import ThreadPoolExecutor


class APIException(Exception):
	pass


class PlagiarismService:
	TEXT_RU_API_KEY = os.getenv('TEXT_RU_API_KEY')

	@staticmethod
	def check_all_services(text, title=None):
		"""Основной метод для проверки через все сервисы"""
		results = {
			"text_ru": PlagiarismService.check_text_ru_wrapper(text, title),
			"duplichecker": PlagiarismService._check_duplichecker(text)
		}
		return results

	@staticmethod
	def check_text_ru_wrapper(text, title=None, max_retries=3, wait_time=20):
		"""
		Обертка для проверки через text.ru с автоматическим ожиданием результатов
		"""
		try:
			# 1. Отправляем текст на проверку
			init_result = PlagiarismService.check_textru(text, title)
			text_uid = init_result['text_uid']

			# 2. Ждем и получаем результаты с несколькими попытками
			for attempt in range(max_retries):
				time.sleep(wait_time)
				result = PlagiarismService.get_textru_result(text_uid)

				if result.get('status') != 'pending':
					return {
						"unique": result.get('unique'),
						"spam": result.get('spam'),
						"water": result.get('water'),
						"seo": result.get('seo'),
						"matches": result.get('matches', []),
						"service": "text.ru"
					}

			raise APIException("Превышено время ожидания результатов от text.ru")

		except Exception as e:
			return {"error": str(e)}

	@staticmethod
	def check_textru(text, title=None):
		"""
		Отправка текста на проверку в text.ru
		Возвращает UID для проверки статуса
		"""
		try:
			post_data = {
				'text': text,
				'userkey': PlagiarismService.TEXT_RU_API_KEY
			}
			if title:
				post_data['title'] = title

			response = requests.post(
				'https://api.text.ru/post',
				data=post_data,
				timeout=10
			)
			response.raise_for_status()
			post_result = response.json()

			if 'error_code' in post_result:
				raise APIException(f"Text.ru API error: {post_result.get('error_desc', 'Unknown error')}")

			return {
				'text_uid': post_result['text_uid'],
				'message': 'Text submitted for analysis'
			}

		except requests.exceptions.RequestException as e:
			raise APIException(f"Ошибка соединения с text.ru: {str(e)}")
		except Exception as e:
			raise APIException(f"Ошибка при работе с text.ru API: {str(e)}")

	@staticmethod
	def get_textru_result(text_uid):
		"""
		Получение результатов проверки из text.ru по UID
		"""
		try:
			params = {
				'uid': text_uid,
				'userkey': PlagiarismService.TEXT_RU_API_KEY,
				'jsonvisible': 'detail'
			}

			response = requests.get(
				'https://api.text.ru/post',
				params=params,
				timeout=10
			)
			response.raise_for_status()
			result = response.json()

			if 'error_code' in result:
				if result['error_code'] == 201:
					return {'status': 'pending'}
				raise APIException(f"Text.ru API error: {result.get('error_desc', 'Unknown error')}")

			return {
				'unique': result.get('text_unique'),
				'spam': result.get('text_spam'),
				'water': result.get('text_water'),
				'seo': result.get('seo_check'),
				'matches': [
					{
						'url': match.get('url'),
						'percent': match.get('percent'),
						'words': match.get('words')
					} for match in result.get('matches', [])
				],
				'status': 'completed'
			}

		except requests.exceptions.RequestException as e:
			raise APIException(f"Ошибка соединения с text.ru: {str(e)}")
		except Exception as e:
			raise APIException(f"Ошибка при получении результатов: {str(e)}")

	@staticmethod
	def _check_duplichecker(text):
		"""
		Проверка через DupliChecker.com (бесплатный, без API ключа)
		"""
		try:
			# 1. Получаем CSRF токен
			session = requests.Session()
			home_page = session.get("https://www.duplichecker.com/")
			soup = BeautifulSoup(home_page.text, 'html.parser')
			csrf_token = soup.find('input', {'name': 'csrf_token'})['value']

			# 2. Отправляем текст на проверку
			check_url = "https://www.duplichecker.com/action/check-plagiarism.php"
			headers = {
				"User-Agent": "Mozilla/5.0",
				"Origin": "https://www.duplichecker.com",
				"Referer": "https://www.duplichecker.com/"
			}
			payload = {
				"csrf_token": csrf_token,
				"paste_text": text,
				"check_plagiarism": ""
			}

			response = session.post(check_url, data=payload, headers=headers)

			# 3. Парсим результаты
			soup = BeautifulSoup(response.text, 'html.parser')
			result_div = soup.find('div', {'class': 'check-results'})

			if not result_div:
				return {"error": "Failed to parse DupliChecker results"}

			# Извлекаем процент уникальности
			unique_percent = 100
			percent_tag = soup.find('span', {'class': 'unique'})
			if percent_tag:
				try:
					unique_percent = float(percent_tag.text.strip('%'))
				except:
					pass

			# Извлекаем источники плагиата
			sources = []
			sources_div = soup.find('div', {'class': 'sources-list'})
			if sources_div:
				for li in sources_div.find_all('li'):
					source = li.find('a')
					if source:
						sources.append({
							"url": source['href'],
							"text": source.text.strip()
						})

			return {
				"score": 100 - unique_percent,
				"matches": sources,
				"unique_percent": unique_percent
			}

		except Exception as e:
			return {"error": str(e)}

	@staticmethod
	def async_check_all(text, title=None):
		"""
		Асинхронная проверка через все сервисы
		"""
		with ThreadPoolExecutor() as executor:
			future_textru = executor.submit(
				PlagiarismService.check_text_ru_wrapper,
				text,
				title
			)
			future_dupli = executor.submit(
				PlagiarismService._check_duplichecker,
				text
			)

			return {
				"text_ru": future_textru.result(),
				"duplichecker": future_dupli.result()
			}
