package by.bsuir.academicauditsystemgateway.repository;

import by.bsuir.academicauditsystemgateway.entity.DocumentAnalyzeRequestOutboxEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DocumentAnalyzeRequestOutboxEventRepository extends JpaRepository<DocumentAnalyzeRequestOutboxEvent, Long> {

    @Query("SELECT e from DocumentAnalyzeRequestOutboxEvent e JOIN FETCH e.analyzeRequest WHERE e.isPublished = false")
    Page<DocumentAnalyzeRequestOutboxEvent> findUnpublishedPage(Pageable pageable);

}
