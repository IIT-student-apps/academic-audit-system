from django.db import models
import uuid
from django.db.models import TextField


class DocumentAnalyzeRequest(models.Model):
	id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
	request_status = models.CharField(max_length=50)
	user_id = models.BigIntegerField()
	document_id = models.CharField(max_length=255)
	report_data = TextField(null=True, blank=True)

	class Meta:
		db_table = 'document_analyze_request'
