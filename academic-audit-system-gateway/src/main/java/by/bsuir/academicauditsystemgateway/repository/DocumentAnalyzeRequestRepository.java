package by.bsuir.academicauditsystemgateway.repository;

import by.bsuir.academicauditsystemgateway.entity.DocumentAnalyzeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface DocumentAnalyzeRequestRepository extends JpaRepository<DocumentAnalyzeRequest, UUID> {

    @Query("SELECT r FROM DocumentAnalyzeRequest r JOIN FETCH r.user")
    List<DocumentAnalyzeRequest> findAllByUserId(Long userId);
}
