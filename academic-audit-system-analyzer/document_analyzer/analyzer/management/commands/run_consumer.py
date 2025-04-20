from django.core.management.base import BaseCommand
from document_analyzer.analyzer.consumers import DocumentAnalyzerConsumer


class Command(BaseCommand):
	help = 'Starts the Kafka consumer for document analysis'

	def handle(self, *args, **options):
		consumer = DocumentAnalyzerConsumer()
		self.stdout.write(self.style.SUCCESS('Starting Kafka consumer...'))
		consumer.start_consuming()
