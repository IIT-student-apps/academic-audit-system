package by.bsuir.academicauditsystemgateway.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "document_analyze_request")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentAnalyzeRequest {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_status")
    private RequestStatus requestStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "document_id")
    private String documentId;

    @Column(name = "report_data")
    private String report;
}
