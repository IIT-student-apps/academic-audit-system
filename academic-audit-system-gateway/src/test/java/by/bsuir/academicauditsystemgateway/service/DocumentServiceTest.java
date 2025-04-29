package by.bsuir.academicauditsystemgateway.service;

import by.bsuir.academicauditsystemgateway.dto.DocumentDto;
import by.bsuir.academicauditsystemgateway.entity.Document;
import by.bsuir.academicauditsystemgateway.entity.DocumentAnalyzeRequest;
import by.bsuir.academicauditsystemgateway.entity.FileFormat;
import by.bsuir.academicauditsystemgateway.exception.DocumentOperationException;
import by.bsuir.academicauditsystemgateway.repository.DocumentRepository;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class DocumentServiceTest {

    @InjectMocks
    private DocumentService documentService;

    @Mock
    private GridFsTemplate gridFsTemplate;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentAnalyzeRequestService requestService;

    @Mock
    private MultipartFile file;

    @Mock
    private GridFSFile gridFSFile;

    private Document document;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        document = Document.builder()
                .documentName("test_document.pdf")
                .fileFormat(FileFormat.PDF)
                .fileId("testFileId")
                .build();
    }

    @Test
    void testSaveDocument() throws IOException {
        InputStream inputStream = new ByteArrayInputStream("test content".getBytes());
        when(file.getInputStream()).thenReturn(inputStream);
        when(file.getOriginalFilename()).thenReturn("test_document.pdf");
        when(file.getContentType()).thenReturn("application/pdf");
        ObjectId objectId = new ObjectId();
        when(gridFsTemplate.store(any(), eq("test_document.pdf"), eq("application/pdf"))).thenReturn(objectId);

        when(documentRepository.save(any())).thenReturn(document);

        Document savedDocument = documentService.saveDocument(file);

        verify(gridFsTemplate).store(any(), eq("test_document.pdf"), eq("application/pdf"));
        verify(documentRepository).save(any());
        assertNotNull(savedDocument);
        assertEquals("test_document.pdf", savedDocument.getDocumentName());
    }

    @Test
    void testGetDocumentDtoWithDocumentContent() throws IOException {
        UUID requestId = UUID.randomUUID();
        DocumentAnalyzeRequest request = new DocumentAnalyzeRequest();
        request.setDocumentId(document.getId());
        when(requestService.getRequestById(requestId)).thenReturn(request);

        when(documentRepository.findById(document.getId())).thenReturn(Optional.of(document));
        when(gridFsTemplate.findOne(any())).thenReturn(gridFSFile);
        when(gridFsTemplate.getResource(gridFSFile)).thenReturn((GridFsResource) new InputStreamResource(new ByteArrayInputStream("document content".getBytes())));

        DocumentDto documentDto = documentService.getDocumentDtoWithDocumentContent(requestId);

        verify(requestService).getRequestById(requestId);
        verify(documentRepository).findById(document.getId());
        assertNotNull(documentDto);
        assertEquals("test_document.pdf", documentDto.getDocumentName());
        assertArrayEquals("document content".getBytes(), documentDto.getFileData());
    }

    @Test
    void testGetDocumentDtoWithDocumentContentThrowsException() throws IOException {
        UUID requestId = UUID.randomUUID();
        when(requestService.getRequestById(requestId)).thenThrow(new DocumentOperationException("Request not found"));

        assertThrows(DocumentOperationException.class, () -> documentService.getDocumentDtoWithDocumentContent(requestId));
    }

    @Test
    void testSaveDocumentThrowsException() throws IOException {
        when(file.getInputStream()).thenThrow(new IOException("File not found"));

        assertThrows(DocumentOperationException.class, () -> documentService.saveDocument(file));
    }

}
