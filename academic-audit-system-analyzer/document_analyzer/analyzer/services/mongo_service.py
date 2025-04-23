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

	def get_all_file_ids(self):
		"""Получить все fileId из коллекции документов"""
		file_ids = []
		for document in self.db['documents'].find({}, {"fileId": 1}):
			file_ids.append(document['fileId'])
		return file_ids

	def get_file_id_by_document_id(self, document_id):
		"""Получить fileId по _id документа"""
		doc = self.db['documents'].find_one(
			{'_id': ObjectId(document_id)},
			{"fileId": 1}
		)
		return doc['fileId'] if doc else None

	def get_document_metadata(self, document_id):
		"""Получить метаданные документа по его _id"""
		return self.db['documents'].find_one({'_id': ObjectId(document_id)})

	def get_file_content(self, file_id):
		"""Получить содержимое файла из GridFS по fileId"""
		grid_out = self.fs.get(ObjectId(file_id))
		return grid_out.read()

	def close(self):
		"""Закрыть соединение с MongoDB"""
		self.client.close()
