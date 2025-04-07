package by.bsuir.academicauditsystemgateway.controller;

import by.bsuir.academicauditsystemgateway.dto.AuthRequestDto;
import by.bsuir.academicauditsystemgateway.dto.AuthResponseDto;
import by.bsuir.academicauditsystemgateway.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authenticationService;

    @RequestMapping(path = "/sign_up", method = RequestMethod.POST)
    public AuthResponseDto signUp(@RequestBody AuthRequestDto request) {
        return authenticationService.signUp(request);
    }

    @RequestMapping(path = "/sign_in", method = RequestMethod.POST)
    public AuthResponseDto signIn(@RequestBody AuthRequestDto request) {
        return authenticationService.signIn(request);
    }
}
