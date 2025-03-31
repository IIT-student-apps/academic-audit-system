package by.bsuir.academicauditsystemgateway.entity;

import lombok.*;
import org.springframework.data.annotation.Id;

@org.springframework.data.mongodb.core.mapping.Document(collection = "documents") // Указываем коллекцию в MongoDB для хранения документов
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @Id
    private String id;

    private String documentName;

    private FileFormat fileFormat;

    private String fileId;

}
