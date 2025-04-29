import os
from abc import ABC, abstractmethod
from typing import Optional, Union
from io import BytesIO
import pdfplumber
import docx
from gridfs import GridOut


class BaseTextExtractor(ABC):
	"""Абстрактный базовый класс для извлечения текста из файлов"""

	@staticmethod
	@abstractmethod
	def extract_text(file_content: bytes) -> str:
		"""Извлекает текст из файла"""
		pass


class PDFTextExtractor(BaseTextExtractor):
	"""Извлекает текст из PDF файлов"""

	@staticmethod
	def extract_text(file_content: bytes) -> str:
		text = []
		with pdfplumber.open(BytesIO(file_content)) as pdf:
			for page in pdf.pages:
				text.append(page.extract_text())
		return "\n".join(text)


class DOCXTextExtractor(BaseTextExtractor):
	"""Извлекает текст из DOCX файлов"""

	@staticmethod
	def extract_text(file_content: bytes) -> str:
		doc = docx.Document(BytesIO(file_content))
		return "\n".join([paragraph.text for paragraph in doc.paragraphs])


class PlainTextExtractor(BaseTextExtractor):
	"""Извлекает текст из простых текстовых файлов"""

	@staticmethod
	def extract_text(file_content: bytes) -> str:
		return file_content.decode('utf-8')


class DocumentProcessor:
	"""Обработчик документов для извлечения текста из различных форматов файлов"""

	# Сопоставление расширений файлов с классами-обработчиками
	EXTRACTORS = {
		'.pdf': PDFTextExtractor,
		'.docx': DOCXTextExtractor,
		'.txt': PlainTextExtractor,
	}

	@staticmethod
	def get_file_extension(filename: str) -> str:
		"""Возвращает расширение файла в нижнем регистре"""
		return os.path.splitext(filename)[1].lower()

	@staticmethod
	def extract_text_from_bytes(file_content: Union[bytes, BytesIO], file_ext: str) -> str:
		"""
		Извлекает текст из байтового содержимого файла

		Args:
			file_content: содержимое файла в виде bytes
			file_ext: расширение файла (.pdf, .docx, .txt)

		Returns:
			Извлеченный текст

		Raises:
			ValueError: если формат файла не поддерживается
		"""
		extractor_class = DocumentProcessor.EXTRACTORS.get(file_ext)
		if extractor_class is None:
			raise ValueError(f"Unsupported file format: {file_ext}")

		return extractor_class.extract_text(file_content)

	@staticmethod
	def get_file_info(file_obj: Union[bytes, GridOut], original_filename: Optional[str] = None) -> tuple[str, str]:
		"""
		Определяет название и расширение файла.

		Args:
			file_obj: содержимое файла (bytes)
			original_filename: оригинальное имя файла (опционально)

		Returns:
			tuple: (filename, extension) где:
				filename - имя файла без расширения
				extension - расширение файла с точкой (например, ".pdf")

		Raises:
			ValueError: если не удалось определить расширение файла
		"""
		# Если имя файла не указано, пытаемся получить из метаданных GridFS
		if original_filename is None and isinstance(file_obj, GridOut):
			original_filename = getattr(file_obj, 'filename', None)

		# Если имя файла все еще не определено, используем имя по умолчанию
		if original_filename is None:
			original_filename = 'document'

		# Получаем базовое имя файла и расширение
		base_name = os.path.basename(original_filename)
		filename, extension = os.path.splitext(base_name)

		# Если расширение не определено, пробуем определить по сигнатуре файла
		if not extension:
			if isinstance(file_obj, bytes):
				# Проверка сигнатур популярных форматов
				if file_obj.startswith(b'%PDF-'):
					extension = '.pdf'
				elif file_obj.startswith(b'PK\x03\x04'):
					extension = '.docx'
				else:
					# По умолчанию считаем текстовым файлом
					extension = '.txt'
			else:
				# Для GridOut без расширения используем .txt по умолчанию
				extension = '.txt'

		# Приводим расширение к нижнему регистру
		extension = extension.lower()

		return filename, extension
