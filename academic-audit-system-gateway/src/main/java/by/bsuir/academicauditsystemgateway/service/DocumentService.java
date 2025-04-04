package by.bsuir.academicauditsystemgateway.service;

import by.bsuir.academicauditsystemgateway.entity.Document;
import by.bsuir.academicauditsystemgateway.entity.FileFormat;
import by.bsuir.academicauditsystemgateway.exception.DocumentOperationException;
import by.bsuir.academicauditsystemgateway.repository.DocumentRepository;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@RequiredArgsConstructor
@Service
public class DocumentService {

    private final GridFsTemplate gridFsTemplate;
    private final DocumentRepository documentRepository;

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

    @SneakyThrows
    public InputStream getFile(String docId) {
        Document document = documentRepository.findById(docId).
                orElseThrow(() -> new DocumentOperationException("Error getting document with id " + docId));
        GridFSFile gridFSFile = gridFsTemplate.findOne(query(where("_id").is(document.getFileId())));
        return gridFsTemplate.getResource(gridFSFile).getInputStream();
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
