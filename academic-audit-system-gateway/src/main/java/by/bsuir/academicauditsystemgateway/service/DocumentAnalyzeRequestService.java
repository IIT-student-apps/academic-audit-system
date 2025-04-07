package by.bsuir.academicauditsystemgateway.service;

import by.bsuir.academicauditsystemgateway.dto.DocumentAnalyzeRequestDto;
import by.bsuir.academicauditsystemgateway.dto.mapper.DocumentAnalyzeRequestMapper;
import by.bsuir.academicauditsystemgateway.entity.*;
import by.bsuir.academicauditsystemgateway.exception.DocumentAnalyzeRequestOperationException;
import by.bsuir.academicauditsystemgateway.exception.RequestNotFoundException;
import by.bsuir.academicauditsystemgateway.repository.DocumentAnalyzeRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DocumentAnalyzeRequestService {

    private final DocumentAnalyzeRequestRepository requestRepository;
    private final UserService userService;
    private final DocumentAnalyzeRequestMapper requestMapper;
    private final DocumentService documentService;
    private final DocumentAnalyzeRequestOutboxEventService outboxEventService;

    @Transactional
    @SneakyThrows
    public DocumentAnalyzeRequestDto createRequest(MultipartFile file, Long userId) {
        User user = userService.findById(userId);

        Document document = documentService.saveDocument(file);

        DocumentAnalyzeRequest request = DocumentAnalyzeRequest.builder()
                .requestStatus(RequestStatus.IN_PROGRESS)
                .user(user)
                .documentId(document.getId())
                .build();

        request = requestRepository.save(request);
        outboxEventService.create(request);

        return requestMapper.toDto(request);
    }

    public List<DocumentAnalyzeRequestDto> getRequestsByUserId(Long userId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return requestRepository.findAllByUserId(userId, pageable)
                .stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    public DocumentAnalyzeRequest getRequestById(UUID requestId) {
        return requestRepository.findById(requestId).
                orElseThrow(() -> new RequestNotFoundException("Request not found with id: " + requestId));
    }

    public DocumentAnalyzeRequestDto getRequestDtoById(UUID id) {
        return requestMapper.toDto(getRequestById(id));
    }

    @Transactional
    public DocumentAnalyzeRequestDto retryDocumentProcessing(UUID requestId) {
        DocumentAnalyzeRequest request = getRequestById(requestId);
        if (request.getRequestStatus() == RequestStatus.FAILED) {
            request.setRequestStatus(RequestStatus.IN_PROGRESS);
            requestRepository.save(request);
            outboxEventService.create(request);
            return requestMapper.toDto(request);
        }
        throw new DocumentAnalyzeRequestOperationException("Can not retry document because it is not in the FAILED status");
    }
}
