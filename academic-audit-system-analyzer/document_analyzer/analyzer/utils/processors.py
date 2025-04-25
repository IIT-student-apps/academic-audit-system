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
	def extract_text(file_content: Union[bytes, BytesIO]) -> str:
		"""Извлекает текст из файла"""
		pass


class PDFTextExtractor(BaseTextExtractor):
	"""Извлекает текст из PDF файлов"""

	@staticmethod
	def extract_text(file_content: Union[bytes, BytesIO]) -> str:
		text = []
		if isinstance(file_content, bytes):
			file_content = BytesIO(file_content)

		with pdfplumber.open(file_content) as pdf:
			for page in pdf.pages:
				text.append(page.extract_text())
		return "\n".join(text)


class DOCXTextExtractor(BaseTextExtractor):
	"""Извлекает текст из DOCX файлов"""

	@staticmethod
	def extract_text(file_content: Union[bytes, BytesIO]) -> str:
		if isinstance(file_content, BytesIO):
			file_content = file_content.getvalue()
		doc = docx.Document(BytesIO(file_content))
		return "\n".join([paragraph.text for paragraph in doc.paragraphs])


class PlainTextExtractor(BaseTextExtractor):
	"""Извлекает текст из простых текстовых файлов"""

	@staticmethod
	def extract_text(file_content: Union[bytes, BytesIO]) -> str:
		if isinstance(file_content, BytesIO):
			file_content = file_content.getvalue()
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
			file_content: содержимое файла в виде bytes или BytesIO
			file_ext: расширение файла (.pdf, .docx и т.д.)

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
	def process_gridfs_file(grid_out: GridOut, original_filename: Optional[str] = None) -> str:
		"""
		Обрабатывает файл из GridFS, извлекая текст непосредственно из байтов

		Args:
			grid_out: объект GridOut из GridFS
			original_filename: оригинальное имя файла (для определения расширения)

		Returns:
			Извлеченный текст

		Raises:
			ValueError: если формат файла не поддерживается
		"""
		# Если имя файла не указано, пытаемся получить из метаданных GridFS
		if original_filename is None:
			original_filename = getattr(grid_out, 'filename', 'document.txt')

		file_ext = DocumentProcessor.get_file_extension(original_filename)

		# Получаем содержимое файла из GridOut
		file_content = grid_out.read()

		# Возвращаем указатель чтения в начало (на случай повторного использования)
		grid_out.seek(0)

		return DocumentProcessor.extract_text_from_bytes(file_content, file_ext)