package by.bsuir.academicauditsystemgateway.service;

import by.bsuir.academicauditsystemgateway.entity.DocumentAnalyzeRequest;
import by.bsuir.academicauditsystemgateway.entity.DocumentAnalyzeRequestOutboxEvent;

public interface DocumentAnalyzeRequestOutboxEventService {
    DocumentAnalyzeRequestOutboxEvent create(DocumentAnalyzeRequest request);
}
