package by.bsuir.academicauditsystemgateway.controller;

import by.bsuir.academicauditsystemgateway.dto.UserDto;
import by.bsuir.academicauditsystemgateway.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user-management")
@RequiredArgsConstructor
public class UserManagementController {
    private final UserService userService;

    @RequestMapping(path = "/user/{userId}", method = RequestMethod.GET)
    public UserDto getUserById(@PathVariable Long userId) {
        return userService.getDtoById(userId);
    }

    @RequestMapping(path = "/create-user", method = RequestMethod.POST)
    public UserDto createUser(@RequestBody UserDto user) {
        return userService.createUser(user);
    }

    @RequestMapping(path = "/user/{userId}", method = RequestMethod.PATCH)
    public UserDto updateUser(@RequestBody UserDto user, @PathVariable("userId") Long userId) {
        return userService.updateUser(userId, user);
    }

    @RequestMapping(path = "/user/{userId}", method = RequestMethod.DELETE)
    public UserDto deleteUser(@PathVariable Long userId) {
        return userService.deleteUser(userId);
    }
}
