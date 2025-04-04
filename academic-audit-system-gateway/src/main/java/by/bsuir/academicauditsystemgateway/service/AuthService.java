package by.bsuir.academicauditsystemgateway.service;


import by.bsuir.academicauditsystemgateway.dto.AuthRequestDto;
import by.bsuir.academicauditsystemgateway.dto.AuthResponseDto;
import by.bsuir.academicauditsystemgateway.dto.UserDto;
import by.bsuir.academicauditsystemgateway.dto.mapper.UserMapper;
import by.bsuir.academicauditsystemgateway.entity.User;
import by.bsuir.academicauditsystemgateway.entity.UserRole;
import by.bsuir.academicauditsystemgateway.exception.UserOperationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponseDto signUp(AuthRequestDto authRequestDto) {
        if(userService.existsByLogin(authRequestDto.getLogin())) {
            throw new UserOperationException("User already exists with login: " + authRequestDto.getLogin());
        }
        User user = userMapper.fromAuthRequestDto(authRequestDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(UserRole.ROLE_USER);
        user = userService.createUser(user);
        String token = jwtService.generateToken(user);
        return new AuthResponseDto(token);
    }

    public AuthResponseDto signIn(AuthRequestDto request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getLogin(),
                request.getPassword()
        ));

        UserDetails user = userService.getByLogin(request.getLogin());
        String jwt = jwtService.generateToken(user);
        return new AuthResponseDto(jwt);
    }
}
