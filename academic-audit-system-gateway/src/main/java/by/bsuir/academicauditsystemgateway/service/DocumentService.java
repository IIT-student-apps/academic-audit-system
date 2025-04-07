package by.bsuir.academicauditsystemgateway.service;

import by.bsuir.academicauditsystemgateway.dto.DocumentDto;
import by.bsuir.academicauditsystemgateway.entity.Document;
import by.bsuir.academicauditsystemgateway.entity.DocumentAnalyzeRequest;
import by.bsuir.academicauditsystemgateway.entity.FileFormat;
import by.bsuir.academicauditsystemgateway.exception.DocumentNotFoundException;
import by.bsuir.academicauditsystemgateway.exception.DocumentOperationException;
import by.bsuir.academicauditsystemgateway.repository.DocumentRepository;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
public class DocumentService {

    private final GridFsTemplate gridFsTemplate;
    private final DocumentRepository documentRepository;
    private final DocumentAnalyzeRequestService requestService;

    public DocumentService(GridFsTemplate gridFsTemplate, DocumentRepository documentRepository, @Lazy DocumentAnalyzeRequestService requestService) {
        this.gridFsTemplate = gridFsTemplate;
        this.documentRepository = documentRepository;
        this.requestService = requestService;
    }

    public Document saveDocument(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            FileFormat fileFormat = mapMimeTypeToEnum(file.getContentType());
            ObjectId fileId = gridFsTemplate.store(inputStream, file.getOriginalFilename(), file.getContentType());
            Document document = Document.builder()
                    .documentName(file.getOriginalFilename())
                    .fileFormat(fileFormat)
                    .fileId(fileId.toString())
                    .build();
            documentRepository.save(document);
            return document;
        } catch (Exception e) {
            throw new DocumentOperationException("Error saving document", e);
        }
    }

    @Transactional
    public DocumentDto getDocumentDtoWithDocumentContent(UUID requestId) {
        DocumentAnalyzeRequest request = requestService.getRequestById(requestId);
        Document document = documentRepository.findById(request.getDocumentId())
                .orElseThrow(() -> new DocumentOperationException("Error getting document with id " + requestId));
        GridFSFile gridFSFile = gridFsTemplate.findOne(query(where("_id").is(document.getFileId())));
        try (InputStream fileStream = gridFsTemplate.getResource(gridFSFile).getInputStream()) {
            byte[] fileData = IOUtils.toByteArray(fileStream);
            return new DocumentDto(document.getDocumentName(), fileData);
        } catch (IOException e) {
            throw new DocumentOperationException("Error downloading document with id " + document.getFileId(), e);
        }
    }

    private FileFormat mapMimeTypeToEnum(String mimeType) {
        return switch (mimeType) {
            case "application/pdf" -> FileFormat.PDF;
            case "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ->
                    FileFormat.DOCX;
            case "text/plain" -> FileFormat.TXT;
            default -> throw new IllegalArgumentException("Unsupported file format: " + mimeType);
        };
    }
}
