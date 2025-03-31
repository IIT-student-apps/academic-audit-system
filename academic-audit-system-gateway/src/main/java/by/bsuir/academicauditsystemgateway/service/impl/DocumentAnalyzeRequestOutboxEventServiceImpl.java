package by.bsuir.academicauditsystemgateway.service.impl;

import by.bsuir.academicauditsystemgateway.entity.DocumentAnalyzeRequest;
import by.bsuir.academicauditsystemgateway.entity.DocumentAnalyzeRequestOutboxEvent;
import by.bsuir.academicauditsystemgateway.repository.DocumentAnalyzeRequestOutboxEventRepository;
import by.bsuir.academicauditsystemgateway.service.DocumentAnalyzeRequestOutboxEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentAnalyzeRequestOutboxEventServiceImpl implements DocumentAnalyzeRequestOutboxEventService {

    private final DocumentAnalyzeRequestOutboxEventRepository repository;

    @Override
    public DocumentAnalyzeRequestOutboxEvent create(DocumentAnalyzeRequest request) {
        return repository.save(DocumentAnalyzeRequestOutboxEvent.builder()
                .analyzeRequest(request)
                .isPublished(false)
                .build());
    }
}
