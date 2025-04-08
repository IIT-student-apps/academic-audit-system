package by.bsuir.academicauditsystemgateway.service;

import by.bsuir.academicauditsystemgateway.dto.DocumentAnalyzeRequestDto;
import by.bsuir.academicauditsystemgateway.entity.*;
import by.bsuir.academicauditsystemgateway.exception.DocumentAnalyzeRequestOperationException;
import by.bsuir.academicauditsystemgateway.repository.DocumentAnalyzeRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class DocumentAnalyzeRequestServiceTest {

    @InjectMocks
    private DocumentAnalyzeRequestService documentAnalyzeRequestService;

    @Mock
    private DocumentAnalyzeRequestRepository requestRepository;

    @Mock
    private UserService userService;

    @Mock
    private DocumentService documentService;

    @Mock
    private DocumentAnalyzeRequestOutboxEventService outboxEventService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        user.setLogin("testuser");
    }

    @Test
    void testCreateRequest() {
        DocumentAnalyzeRequestDto requestDto = documentAnalyzeRequestService.createRequest(mock(MultipartFile.class), 1L);
        assertNotNull(requestDto);
        verify(requestRepository, times(1)).save(any());
    }

    @Test
    void testRetryDocumentProcessingFailedRequest() {
        DocumentAnalyzeRequest request = new DocumentAnalyzeRequest();
        request.setRequestStatus(RequestStatus.FAILED);
        when(requestRepository.findById(any())).thenReturn(Optional.of(request));

        DocumentAnalyzeRequestDto result = documentAnalyzeRequestService.retryDocumentProcessing(UUID.randomUUID());
        assertEquals(RequestStatus.IN_PROGRESS, result.getRequestStatus());
    }

    @Test
    void testRetryDocumentProcessingNonFailedRequest() {
        DocumentAnalyzeRequest request = new DocumentAnalyzeRequest();
        request.setRequestStatus(RequestStatus.IN_PROGRESS);
        when(requestRepository.findById(any())).thenReturn(Optional.of(request));

        assertThrows(DocumentAnalyzeRequestOperationException.class, () -> documentAnalyzeRequestService.retryDocumentProcessing(UUID.randomUUID()));
    }
}
