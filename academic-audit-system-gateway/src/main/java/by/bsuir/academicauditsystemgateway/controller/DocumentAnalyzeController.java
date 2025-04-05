package by.bsuir.academicauditsystemgateway.controller;

import by.bsuir.academicauditsystemgateway.dto.DocumentAnalyzeRequestDto;
import by.bsuir.academicauditsystemgateway.service.DocumentAnalyzeRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/document-analyze")
@RequiredArgsConstructor
public class DocumentAnalyzeController {

    private final DocumentAnalyzeRequestService requestService;

    @RequestMapping(method = RequestMethod.POST, path = "/upload-document")
    public DocumentAnalyzeRequestDto createRequest(@RequestParam MultipartFile file, @RequestAttribute Long userId) {
        return requestService.createRequest(file, userId);
    }

    @RequestMapping(method = RequestMethod.PATCH, path = "/retry-processing/{requestId}")
    public DocumentAnalyzeRequestDto retryProcessing(@PathVariable UUID requestId) {
        return requestService.retryDocumentProcessing(requestId);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/get-my-requests-page")
    public List<DocumentAnalyzeRequestDto> getRequestsPage(@RequestAttribute Long userId, @RequestParam Integer page,
                                                           @RequestParam Integer size) {
        return requestService.getRequestsByUserId(userId, page, size);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/requests/{requestId}")
    public DocumentAnalyzeRequestDto getRequest(@PathVariable UUID requestId) {
        return requestService.getRequestDtoById(requestId);
    }
}
