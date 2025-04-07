package by.bsuir.academicauditsystemgateway.service;

import by.bsuir.academicauditsystemgateway.dto.DocumentAnalyzeRequestDto;
import by.bsuir.academicauditsystemgateway.entity.DocumentAnalyzeRequestOutboxEvent;
import by.bsuir.academicauditsystemgateway.repository.DocumentAnalyzeRequestOutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OutboxEventService {
    private static final int OFFSET = 0;
    private static final int LIMIT = 20;

    private final DocumentAnalyzeRequestOutboxEventRepository outboxRepository;
    private final KafkaTemplate<String, DocumentAnalyzeRequestDto> kafkaTemplate;

    @Value("spring.kafka.template.default-topic")
    private String kafkaTopic;

    @Transactional
    public void sendUnsentEvents() {
        Pageable pageable = PageRequest.of(OFFSET, LIMIT);
        Page<DocumentAnalyzeRequestOutboxEvent> eventsPage = outboxRepository.findUnpublishedPage(pageable);

        for (DocumentAnalyzeRequestOutboxEvent event : eventsPage) {
            var request = event.getAnalyzeRequest();
            var dto = DocumentAnalyzeRequestDto.builder()
                    .id(request.getId())
                    .requestStatus(request.getRequestStatus())
                    .userId(request.getUser().getId())
                    .documentId(request.getDocumentId())
                    .report(request.getReport())
                    .build();

            kafkaTemplate.send(kafkaTopic, dto.getId().toString(), dto);
            event.setPublished(true);
            outboxRepository.save(event);
        }
    }
}
