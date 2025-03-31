package by.bsuir.academicauditsystemgateway.repository;

import by.bsuir.academicauditsystemgateway.entity.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends MongoRepository<Document, String> {
}
