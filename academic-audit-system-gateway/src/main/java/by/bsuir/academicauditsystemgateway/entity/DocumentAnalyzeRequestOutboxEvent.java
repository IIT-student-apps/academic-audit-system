package by.bsuir.academicauditsystemgateway.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DocumentAnalyzeRequestOutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_published", nullable = false)
    private boolean isPublished;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "request_id", nullable = false)
    private DocumentAnalyzeRequest analyzeRequest;


}
