import os
import PyPDF2
from docx import Document
from mimetypes import guess_type


class DocumentProcessor:
	@staticmethod
	def get_file_extension(file_path):
		"""Определяет расширение файла"""
		# Первый вариант: по расширению файла
		ext = os.path.splitext(file_path)[1].lower()
		if ext == '.pdf':
			return 'pdf'
		elif ext == '.docx':
			return 'docx'
		elif ext == '.txt':
			return 'txt'

		# Второй вариант: по MIME-типу (если первый не сработал)
		mime_type, _ = guess_type(file_path)
		if mime_type == 'application/pdf':
			return 'pdf'
		elif mime_type in ['application/vnd.openxmlformats-officedocument.wordprocessingml.document',
						   'application/msword']:
			return 'docx'
		elif mime_type == 'text/plain':
			return 'txt'

		raise ValueError(f"Unsupported file type: {file_path}")

	@staticmethod
	def extract_text(file_path, file_type=None):
		"""Извлекает текст из файла"""
		if file_type is None:
			file_type = DocumentProcessor.get_file_extension(file_path)

		if file_type == 'pdf':
			return DocumentProcessor._extract_from_pdf(file_path)
		elif file_type == 'docx':
			return DocumentProcessor._extract_from_docx(file_path)
		elif file_type == 'txt':
			return DocumentProcessor._extract_from_txt(file_path)
		else:
			raise ValueError(f"Unsupported file type: {file_type}")

	@staticmethod
	def _extract_from_pdf(file_path):
		"""Извлечение текста из PDF"""
		text = ""
		with open(file_path, "rb") as f:
			reader = PyPDF2.PdfReader(f)
			for page in reader.pages:
				text += page.extract_text()
		return text

	@staticmethod
	def _extract_from_docx(file_path):
		"""Извлечение текста из DOCX"""
		doc = Document(file_path)
		return "\n".join([para.text for para in doc.paragraphs])

	@staticmethod
	def _extract_from_txt(file_path):
		"""Извлечение текста из TXT"""
		with open(file_path, "r", encoding="utf-8") as f:
			return f.read()