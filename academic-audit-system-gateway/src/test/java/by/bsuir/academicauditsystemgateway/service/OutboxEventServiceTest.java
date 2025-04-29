package by.bsuir.academicauditsystemgateway.service;

import by.bsuir.academicauditsystemgateway.dto.DocumentAnalyzeRequestDto;
import by.bsuir.academicauditsystemgateway.entity.DocumentAnalyzeRequestOutboxEvent;
import by.bsuir.academicauditsystemgateway.repository.DocumentAnalyzeRequestOutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;

import static org.mockito.Mockito.*;

@SpringBootTest
class OutboxEventServiceTest {

    @InjectMocks
    private OutboxEventService outboxEventService;

    @Mock
    private DocumentAnalyzeRequestOutboxEventRepository outboxRepository;

    @Mock
    private KafkaTemplate<String, DocumentAnalyzeRequestDto> kafkaTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendUnsentEvents() {
        DocumentAnalyzeRequestOutboxEvent event = new DocumentAnalyzeRequestOutboxEvent();
        event.setPublished(false);
        when(outboxRepository.findUnpublishedPage(any())).thenReturn((Page<DocumentAnalyzeRequestOutboxEvent>) List.of(event));

        outboxEventService.sendUnsentEvents();

        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any());
        verify(outboxRepository, times(1)).save(event);
    }
}
