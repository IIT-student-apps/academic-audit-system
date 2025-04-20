from pymongo import MongoClient
from gridfs import GridFS


class MongoService:
	def __init__(self):
		self.client = MongoClient('mongodb://root:root@mongo:27017/')
		self.db = self.client['academic_audit_system_db']
		self.fs = GridFS(self.db)

	def get_document_metadata(self, document_id):
		return self.db['documents'].find_one({'_id': document_id})

	def get_file_content(self, file_id):
		grid_out = self.fs.get(file_id)
		return grid_out.read()

	def close(self):
		self.client.close()
