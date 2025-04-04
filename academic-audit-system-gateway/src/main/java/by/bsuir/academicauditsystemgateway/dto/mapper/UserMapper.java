package by.bsuir.academicauditsystemgateway.dto.mapper;

import by.bsuir.academicauditsystemgateway.dto.AuthRequestDto;
import by.bsuir.academicauditsystemgateway.entity.User;
import org.mapstruct.Mapper;

@Mapper
public interface UserMapper {
    User fromAuthRequestDto(AuthRequestDto authRequestDto);
}
