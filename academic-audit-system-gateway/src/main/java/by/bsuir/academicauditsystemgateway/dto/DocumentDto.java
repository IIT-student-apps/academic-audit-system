package by.bsuir.academicauditsystemgateway.dto;

import by.bsuir.academicauditsystemgateway.entity.FileFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DocumentDto {

    private String documentName;
    private FileFormat fileFormat;
    private byte[] fileData;

}
