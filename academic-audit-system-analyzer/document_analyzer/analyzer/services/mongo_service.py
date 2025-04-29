from pymongo import MongoClient
from gridfs import GridFS
from bson import ObjectId


class MongoService:
	def __init__(self):
		self.client = MongoClient(
			'mongodb://root:root@mongo:27017/academic_audit_system_db?authSource=admin'
		)
		self.db = self.client['academic_audit_system_db']
		self.fs = GridFS(self.db)

	def get_document_metadata(self, document_id):
		"""Получить метаданные документа по его _id"""
		return self.db['documents'].find_one({'_id': ObjectId(document_id)})

	def get_file_content(self, file_id):
		grid_out = self.fs.get(ObjectId(file_id))
		return grid_out.read()
