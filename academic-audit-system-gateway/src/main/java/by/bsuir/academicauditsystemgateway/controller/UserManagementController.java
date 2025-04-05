package by.bsuir.academicauditsystemgateway.controller;

import by.bsuir.academicauditsystemgateway.entity.User;
import by.bsuir.academicauditsystemgateway.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user_management")
@RequiredArgsConstructor
public class UserManagementController {
    private final UserService userService;

    @RequestMapping(path = "/user/{userId}", method = RequestMethod.GET)
    public User getUserById(@PathVariable Long userId) {
        return userService.findById(userId);
    }

    @RequestMapping(path = "/create_user", method = RequestMethod.POST)
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @RequestMapping(path = "/user/{userId}", method = RequestMethod.PATCH)
    public User updateUser(@RequestBody User user, @PathVariable("userId") Long userId) {
        return userService.updateUser(userId, user);
    }

    @RequestMapping(path = "/user/{userId}", method = RequestMethod.DELETE)
    public User deleteUser(@PathVariable Long userId) {
        return userService.deleteUser(userId);
    }
}
