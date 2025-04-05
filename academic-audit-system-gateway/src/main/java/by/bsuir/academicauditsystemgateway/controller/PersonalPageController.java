package by.bsuir.academicauditsystemgateway.controller;


import by.bsuir.academicauditsystemgateway.entity.User;
import by.bsuir.academicauditsystemgateway.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/personal_page")
@RequiredArgsConstructor
public class PersonalPageController {
    private final UserService userService;

    @RequestMapping(path = "/personal_data", method = RequestMethod.GET)
    public User getPersonalInfo(@RequestAttribute Long userId) {
        return userService.findById(userId);
    }

    @RequestMapping(path = "/change_password", method = RequestMethod.PATCH)
    public User changePassword(@RequestAttribute Long userId, @RequestBody User user) {
        return userService.updatePassword(userId, user.getPassword());
    }
}
