import magic, PyPDF2, os
from docx import Document


class DocumentProcessor:
	@staticmethod
	def get_file_extension(file_path):
		"""Определяет расширение файла"""
		mime = magic.Magic(mime=True)
		file_type = mime.from_file(file_path)

		if file_type == 'application/pdf':
			return 'pdf'
		elif file_type in ['application/vnd.openxmlformats-officedocument.wordprocessingml.document',
						   'application/msword']:
			return 'docx'
		elif file_type == 'text/plain':
			return 'txt'
		else:
			raise ValueError(f"Unsupported file type: {file_type}")

	@staticmethod
	def extract_text(file_path, file_type):
		"""Извлекает текст из файла в зависимости от его типа"""
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
