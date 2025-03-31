package by.bsuir.academicauditsystemgateway.service.impl;

import by.bsuir.academicauditsystemgateway.entity.User;
import by.bsuir.academicauditsystemgateway.repository.UserRepository;
import by.bsuir.academicauditsystemgateway.service.UserService;
import by.bsuir.academicauditsystemgateway.exception.UserOperationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User createUser(User user) {
        try {
            return userRepository.save(user);
        } catch (Exception e) {
            throw new UserOperationException("Error while creating user", e);
        }
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserOperationException("User not found with id " + id));
    }

    @Override
    public Page<User> getUsersWithPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable);
    }

    @Override
    public User updateUser(Long id, User user) {
        if (!userRepository.existsById(id)) {
            throw new UserOperationException("User not found with id: " + id);
        }

        user.setId(id);
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserOperationException("User not found with id: " + id);
        }

        userRepository.deleteById(id);
    }
}
