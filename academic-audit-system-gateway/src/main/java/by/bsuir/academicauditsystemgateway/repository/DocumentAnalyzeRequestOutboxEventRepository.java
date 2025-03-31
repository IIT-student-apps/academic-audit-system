package by.bsuir.academicauditsystemgateway.repository;

import by.bsuir.academicauditsystemgateway.entity.DocumentAnalyzeRequestOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentAnalyzeRequestOutboxEventRepository extends JpaRepository<DocumentAnalyzeRequestOutboxEvent, Long> {
}
