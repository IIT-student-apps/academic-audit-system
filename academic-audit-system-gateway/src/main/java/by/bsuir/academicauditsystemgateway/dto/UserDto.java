package by.bsuir.academicauditsystemgateway.dto;


import by.bsuir.academicauditsystemgateway.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String login;
    private String password;
    private UserRole role;
}
