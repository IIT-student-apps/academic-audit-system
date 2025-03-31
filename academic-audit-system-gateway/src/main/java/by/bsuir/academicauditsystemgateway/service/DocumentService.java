package by.bsuir.academicauditsystemgateway.service;

import by.bsuir.academicauditsystemgateway.entity.Document;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface DocumentService {

        Document saveDocument(MultipartFile file);

        InputStream getFile(String fileId);

}
