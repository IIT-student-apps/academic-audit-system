import os
from dotenv import load_dotenv
from pymongo import MongoClient
from gridfs import GridFS
from bson import ObjectId

load_dotenv()


class MongoService:
	def __init__(self):
		self.client = MongoClient(
			os.getenv('MONGO_URI')
		)
		self.db = self.client['academic_audit_system_db']
		self.fs = GridFS(self.db)

	def get_document_metadata(self, document_id):
		"""Получить метаданные документа по его _id"""
		return self.db['documents'].find_one({'_id': ObjectId(document_id)})

	def get_file_content(self, file_id):
		grid_out = self.fs.get(ObjectId(file_id))
		return grid_out.read()
