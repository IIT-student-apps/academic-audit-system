import json
import logging
from kafka import KafkaConsumer
from analyzer.services.analysis_service import AnalysisService
from analyzer.services.mongo_service import MongoService
from analyzer.services.postgres_service import PostgresService

# Настройка логгера
logger = logging.getLogger(__name__)
logger.setLevel(logging.INFO)

# Форматтер для логов
formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')

# Консольный обработчик
console_handler = logging.StreamHandler()
console_handler.setFormatter(formatter)
logger.addHandler(console_handler)


class DocumentAnalyzerConsumer:
    def __init__(self):
        logger.info("Initializing DocumentAnalyzerConsumer...")
        try:
            self.consumer = KafkaConsumer(
                'document-analyze-topic',
                bootstrap_servers='kafka:9092',
                group_id='django-consumer-group',
                value_deserializer=lambda x: json.loads(x.decode('utf-8')))
            self.mongo_service = MongoService()
            self.postgres_service = PostgresService()
            self.analysis_service = AnalysisService()
            logger.info("DocumentAnalyzerConsumer initialized successfully")
        except Exception as e:
            logger.error(f"Failed to initialize DocumentAnalyzerConsumer: {str(e)}")
            raise

    def process_message(self, message):
        data = message.value
        logger.info(f"Processing message for document ID: {data.get('documentId')}, request ID: {data.get('id')}")

        try:
            # Шаг 1: Получаем метаданные документа из MongoDB
            logger.debug(f"Fetching metadata for document ID: {data['documentId']}")
            doc_meta = self.mongo_service.get_document_metadata(data['documentId'])
            if not doc_meta:
                error_msg = f"Document metadata not found for ID: {data['documentId']}"
                logger.error(error_msg)
                raise ValueError(error_msg)
            logger.debug("Successfully fetched document metadata")

            # Шаг 2: Получаем содержимое файла из GridFS
            logger.debug(f"Fetching file content with fileId: {doc_meta['fileId']}")
            file_content = self.mongo_service.get_file_content(doc_meta['fileId'])
            logger.debug("Successfully fetched file content")
            logger.debug(f'{file_content}')

            # Шаг 3: Анализируем документ
            logger.info("Starting document analysis")
            report = self.analysis_service.analyze_document(file_content)
            logger.info("Document analysis completed successfully")

            # Шаг 4: Обновляем статус в PostgreSQL
            logger.debug(f"Updating request status to COMPLETED for request ID: {data['id']}")
            self.postgres_service.update_request_status(
                data['id'], report, 'COMPLETED')
            logger.info(f"Successfully processed document ID: {data['documentId']}, request ID: {data['id']}")

        except Exception as e:
            logger.error(
                f"Error processing document ID: {data.get('documentId')}, request ID: {data.get('id')}: {str(e)}",
                exc_info=True)
            # В случае ошибки обновляем статус на FAILED
            self.postgres_service.update_request_status(
                data['id'], str(e), 'FAILED')
            logger.info(f"Updated request status to FAILED for request ID: {data['id']}")

    def start_consuming(self):
        logger.info("Starting Kafka consumer...")
        try:
            for message in self.consumer:
                logger.debug(f"Received new message from Kafka: {message}")
                self.process_message(message)
        except Exception as e:
            logger.error(f"Error in Kafka consumer: {str(e)}", exc_info=True)
            raise
        finally:
            logger.info("Kafka consumer stopped")
