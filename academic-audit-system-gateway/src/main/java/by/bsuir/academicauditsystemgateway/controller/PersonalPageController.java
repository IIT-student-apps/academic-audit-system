package by.bsuir.academicauditsystemgateway.controller;


import by.bsuir.academicauditsystemgateway.dto.UserDto;
import by.bsuir.academicauditsystemgateway.entity.User;
import by.bsuir.academicauditsystemgateway.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/personal-page")
@RequiredArgsConstructor
public class PersonalPageController {
    private final UserService userService;

    @RequestMapping(path = "/personal-data", method = RequestMethod.GET)
    public UserDto getPersonalInfo(@RequestAttribute Long userId) {
        return userService.getDtoById(userId);
    }

    @RequestMapping(path = "/change_password", method = RequestMethod.PATCH)
    public User changePassword(@RequestAttribute Long userId, @RequestBody UserDto user) {
        return userService.updatePassword(userId, user.getPassword());
    }
}
