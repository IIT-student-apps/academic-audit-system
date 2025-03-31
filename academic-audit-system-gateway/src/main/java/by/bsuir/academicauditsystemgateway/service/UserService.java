package by.bsuir.academicauditsystemgateway.service;

import by.bsuir.academicauditsystemgateway.entity.User;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface UserService {
    User createUser(User user);
    User findById(Long id);
    public Page<User> getUsersWithPagination(int page, int size);
    User updateUser(Long id, User user);
    void deleteUser(Long id);
}
