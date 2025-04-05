package by.bsuir.academicauditsystemgateway.service;

import by.bsuir.academicauditsystemgateway.dto.DocumentAnalyzeRequestDto;
import by.bsuir.academicauditsystemgateway.dto.mapper.DocumentAnalyzeRequestMapper;
import by.bsuir.academicauditsystemgateway.entity.*;
import by.bsuir.academicauditsystemgateway.repository.DocumentAnalyzeRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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

    public DocumentAnalyzeRequestDto findById(UUID id) {
        DocumentAnalyzeRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found with id: " + id));

        return requestMapper.toDto(request);
    }

    public List<DocumentAnalyzeRequestDto> getRequestsByUserId(Long userId,  Integer page, Integer size) {
        return requestRepository.findAllByUserId(userId)
                .stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    public DocumentAnalyzeRequestDto retryDocumentProcessing(UUID requestId) {
        return null;
    }
}
