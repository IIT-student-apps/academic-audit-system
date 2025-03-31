package by.bsuir.academicauditsystemgateway.service;

import by.bsuir.academicauditsystemgateway.dto.DocumentAnalyzeRequestDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentAnalyzeRequestService {
    DocumentAnalyzeRequestDto createRequest(MultipartFile file, Long userId);

    DocumentAnalyzeRequestDto getRequest(Long id);

    List<DocumentAnalyzeRequestDto> getRequestsByUserId(Long userId, Integer page, Integer size);
}