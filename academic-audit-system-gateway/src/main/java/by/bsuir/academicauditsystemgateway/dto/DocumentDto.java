package by.bsuir.academicauditsystemgateway.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DocumentDto {

    private String documentName;
    private byte[] fileData;

}
