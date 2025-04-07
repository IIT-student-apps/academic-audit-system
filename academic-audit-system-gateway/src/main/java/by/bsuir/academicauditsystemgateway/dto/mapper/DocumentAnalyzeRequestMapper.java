package by.bsuir.academicauditsystemgateway.dto.mapper;

import by.bsuir.academicauditsystemgateway.dto.DocumentAnalyzeRequestDto;
import by.bsuir.academicauditsystemgateway.entity.DocumentAnalyzeRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DocumentAnalyzeRequestMapper {

    @Mapping(source = "user.id", target = "userId")
    DocumentAnalyzeRequestDto toDto(DocumentAnalyzeRequest entity);

    @Mapping(source = "userId", target = "user.id")
    DocumentAnalyzeRequest toEntity(DocumentAnalyzeRequestDto dto);
}
