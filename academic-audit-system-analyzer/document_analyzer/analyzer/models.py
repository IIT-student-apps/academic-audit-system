from django.db import models
import uuid


class DocumentAnalyzeRequest(models.Model):
	id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
	request_status = models.CharField(max_length=50)
	user_id = models.BigIntegerField()
	document_id = models.CharField(max_length=255)
	report_data = models.TextField(null=True, blank=True)

	class Meta:
		db_table = 'document_analyze_request'
