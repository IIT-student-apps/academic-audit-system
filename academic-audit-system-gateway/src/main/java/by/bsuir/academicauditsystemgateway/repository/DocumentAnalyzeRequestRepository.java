package by.bsuir.academicauditsystemgateway.repository;

import by.bsuir.academicauditsystemgateway.entity.DocumentAnalyzeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DocumentAnalyzeRequestRepository extends JpaRepository<DocumentAnalyzeRequest, Long> {

    @Query("SELECT r FROM DocumentAnalyzeRequest r JOIN FETCH r.user")
    List<DocumentAnalyzeRequest> findAllByUserId(Long userId);
}
