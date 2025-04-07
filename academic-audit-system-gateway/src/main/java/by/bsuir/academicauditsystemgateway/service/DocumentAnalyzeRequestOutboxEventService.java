package by.bsuir.academicauditsystemgateway.service;

import by.bsuir.academicauditsystemgateway.entity.DocumentAnalyzeRequest;
import by.bsuir.academicauditsystemgateway.entity.DocumentAnalyzeRequestOutboxEvent;
import by.bsuir.academicauditsystemgateway.repository.DocumentAnalyzeRequestOutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentAnalyzeRequestOutboxEventService {

    private final DocumentAnalyzeRequestOutboxEventRepository repository;

    public DocumentAnalyzeRequestOutboxEvent create(DocumentAnalyzeRequest request) {
        return repository.save(DocumentAnalyzeRequestOutboxEvent.builder()
                .analyzeRequest(request)
                .isPublished(false)
                .build());
    }
}
