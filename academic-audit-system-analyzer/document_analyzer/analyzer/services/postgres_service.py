from django.db import transaction
from analyzer.models import DocumentAnalyzeRequest


class PostgresService:
    @staticmethod
    def update_request_status(request_id, report, status):
        with transaction.atomic():
            request = DocumentAnalyzeRequest.objects.get(id=request_id)
            request.report_data = report
            request.request_status = status
            request.save()
