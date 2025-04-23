import json
from kafka import KafkaConsumer
from analyzer.services.analysis_service import AnalysisService
from analyzer.services.mongo_service import MongoService
from analyzer.services.postgres_service import PostgresService


class DocumentAnalyzerConsumer:
    def __init__(self):
        self.consumer = KafkaConsumer(
            'spring.kafka.template.default-topic',
            bootstrap_servers='kafka:9092',
            group_id='django-consumer-group',
            value_deserializer=lambda x: json.loads(x.decode('utf-8')))
        self.mongo_service = MongoService()
        self.postgres_service = PostgresService()
        self.analysis_service = AnalysisService()

    def process_message(self, message):
        data = message.value
        try:
            # Шаг 1: Получаем метаданные документа из MongoDB
            doc_meta = self.mongo_service.get_document_metadata(data['documentId'])
            if not doc_meta:
                raise ValueError("Document metadata not found")

            # Шаг 2: Получаем содержимое файла из GridFS
            file_content = self.mongo_service.get_file_content(doc_meta['fileId'])

            # Шаг 3: Анализируем документ
            report = self.analysis_service.analyze_document(file_content)

            # Шаг 4: Обновляем статус в PostgreSQL
            self.postgres_service.update_request_status(
                data['id'], report, 'COMPLETED')

        except Exception as e:
            # В случае ошибки обновляем статус на FAILED
            self.postgres_service.update_request_status(
                data['id'], str(e), 'FAILED')

    def start_consuming(self):
        for message in self.consumer:
            self.process_message(message)
