package by.bsuir.academicauditsystemgateway.dto;

import by.bsuir.academicauditsystemgateway.entity.RequestStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentAnalyzeRequestDto {
    private Long id;
    private RequestStatus requestStatus;
    private Long userId;
    private String documentId;
    private String report;
}
