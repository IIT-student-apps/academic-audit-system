package by.bsuir.academicauditsystemgateway.controller;

import by.bsuir.academicauditsystemgateway.dto.DocumentAnalyzeRequestDto;
import by.bsuir.academicauditsystemgateway.service.DocumentAnalyzeRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(path = "/document-analyze")
@RequiredArgsConstructor
public class DocumentAnalyzeController {

    private final DocumentAnalyzeRequestService requestService;

    @RequestMapping(method = RequestMethod.POST, path = "/upload-document")
    public DocumentAnalyzeRequestDto createRequest(@RequestParam MultipartFile file, @RequestAttribute(required = false) Long userId) {
        System.out.println(file.getContentType());
        return requestService.createRequest(file, 1L);
    }


}
