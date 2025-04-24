import os
import logging
import PyPDF2
from docx import Document
from typing import Optional, BinaryIO
import tempfile

# Настройка логирования
logging.basicConfig(
	level=logging.INFO,
	format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger('DocumentProcessor')


class DocumentProcessor:
	@staticmethod
	def get_file_extension(file_path: str) -> str:
		"""Определяет расширение файла по сигнатуре и расширению"""
		logger.info(f"Определение типа файла: {file_path}")

		if not os.path.exists(file_path):
			error_msg = f"Файл не найден: {file_path}"
			logger.error(error_msg)
			raise FileNotFoundError(error_msg)

		# Сначала проверяем сигнатуру файла
		try:
			with open(file_path, 'rb') as f:
				header = f.read(8)  # Читаем первые 8 байт

				# PDF
				if header.startswith(b'%PDF'):
					return 'pdf'

				# DOCX (ZIP-архив)
				if header.startswith(b'PK\x03\x04'):
					# Дополнительная проверка на DOCX
					f.seek(0)
					zip_header = f.read(30)
					if b'word/document.xml' in zip_header or b'[Content_Types].xml' in zip_header:
						return 'docx'

				# TXT (пробуем декодировать как ASCII)
				try:
					header.decode('ascii')
					return 'txt'
				except UnicodeDecodeError:
					pass
		except Exception as e:
			logger.warning(f"Ошибка анализа файла: {str(e)}")

		# Если по сигнатуре не определили, проверяем расширение
		ext = os.path.splitext(file_path)[1].lower()
		logger.debug(f"Определение по расширению: {ext}")

		if ext == '.pdf':
			return 'pdf'
		elif ext == '.docx':
			return 'docx'
		elif ext == '.txt':
			return 'txt'

		error_msg = f"Неподдерживаемый тип файла: {file_path}"
		logger.error(error_msg)
		raise ValueError(error_msg)

	@staticmethod
	def extract_text(file_path: str, file_type: Optional[str] = None) -> str:
		"""Извлекает текст из файла с улучшенной обработкой ошибок"""
		logger.info(f"Начало обработки файла: {file_path}")
		try:
			if file_type is None:
				file_type = DocumentProcessor.get_file_extension(file_path)
				logger.debug(f"Тип файла определен как: {file_type}")

			with open(file_path, 'rb') as f:
				if file_type == 'pdf':
					logger.debug("Обработка PDF файла")
					return DocumentProcessor._extract_from_pdf(f)
				elif file_type == 'docx':
					logger.debug("Обработка DOCX файла")
					return DocumentProcessor._extract_from_docx(f)
				elif file_type == 'txt':
					logger.debug("Обработка TXT файла")
					return DocumentProcessor._extract_from_txt(f)
				else:
					error_msg = f"Неподдерживаемый тип файла: {file_type}"
					logger.error(error_msg)
					raise ValueError(error_msg)
		except Exception as e:
			error_msg = f"Ошибка обработки файла {file_path}: {str(e)}"
			logger.error(error_msg, exc_info=True)
			raise RuntimeError(error_msg) from e
		finally:
			logger.info(f"Завершение обработки файла: {file_path}")

	@staticmethod
	def _extract_from_pdf(file_obj: BinaryIO) -> str:
		"""Извлечение текста из PDF"""
		try:
			logger.debug("Чтение PDF файла")
			reader = PyPDF2.PdfReader(file_obj)

			if reader.is_encrypted:
				logger.warning("PDF файл зашифрован, попытка чтения без пароля")
				try:
					reader.decrypt('')
					logger.info("PDF файл успешно открыт без пароля")
				except:
					error_msg = "Не удалось прочитать зашифрованный PDF"
					logger.error(error_msg)
					raise RuntimeError(error_msg)

			text = ""
			page_count = len(reader.pages)
			logger.debug(f"Найдено {page_count} страниц в PDF")

			for i, page in enumerate(reader.pages, 1):
				page_text = page.extract_text()
				if page_text:
					text += page_text
				logger.debug(f"Обработана страница {i}/{page_count}")

			if not text.strip():
				error_msg = "PDF не содержит извлекаемого текста (возможно изображения)"
				logger.warning(error_msg)
				raise RuntimeError(error_msg)

			logger.debug(f"Успешно извлечено {len(text)} символов из PDF")
			return text
		except PyPDF2.PdfReadError as e:
			error_msg = f"Ошибка чтения PDF: {str(e)}"
			logger.error(error_msg)
			raise RuntimeError(error_msg) from e

	@staticmethod
	def _extract_from_docx(file_obj: BinaryIO) -> str:
		"""Извлечение текста из DOCX"""
		tmp_path = None
		try:
			logger.debug("Чтение DOCX файла")

			# Создаем временный файл для docx.Document
			with tempfile.NamedTemporaryFile(delete=False) as tmp:
				tmp.write(file_obj.read())
				tmp_path = tmp.name
				logger.debug(f"Создан временный файл: {tmp_path}")

			doc = Document(tmp_path)
			full_text = []

			# Обработка параграфов
			paragraphs = doc.paragraphs
			logger.debug(f"Найдено {len(paragraphs)} параграфов")
			for para in paragraphs:
				if para.text.strip():
					full_text.append(para.text)

			# Обработка таблиц
			tables = doc.tables
			logger.debug(f"Найдено {len(tables)} таблиц")
			for table in tables:
				for row in table.rows:
					for cell in row.cells:
						if cell.text.strip():
							full_text.append(cell.text)

			text = "\n".join(full_text)

			if not text.strip():
				error_msg = "DOCX файл пуст или не содержит извлекаемого текста"
				logger.warning(error_msg)
				raise RuntimeError(error_msg)

			logger.debug(f"Успешно извлечено {len(text)} символов из DOCX")
			return text
		except Exception as e:
			error_msg = f"Ошибка чтения DOCX: {str(e)}"
			logger.error(error_msg, exc_info=True)
			raise RuntimeError(error_msg) from e
		finally:
			if tmp_path and os.path.exists(tmp_path):
				try:
					os.unlink(tmp_path)
					logger.debug(f"Временный файл {tmp_path} удален")
				except Exception as e:
					logger.warning(f"Ошибка удаления временного файла: {str(e)}")

	@staticmethod
	def _extract_from_txt(file_obj: BinaryIO) -> str:
		"""Извлечение текста из TXT с попыткой разных кодировок"""
		raw_data = file_obj.read()

		# Список кодировок для попытки (можно расширить)
		encodings = ['utf-8', 'utf-16', 'cp1251', 'cp1252', 'iso-8859-1', 'koi8-r']

		for enc in encodings:
			try:
				return raw_data.decode(enc)
			except UnicodeDecodeError:
				continue

		# Если ничего не сработало, пробуем с заменой ошибок
		try:
			return raw_data.decode('utf-8', errors='replace')
		except Exception as e:
			error_msg = f"Не удалось декодировать текстовый файл: {str(e)}"
			logger.error(error_msg)
			raise RuntimeError(error_msg)
