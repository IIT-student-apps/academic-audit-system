package by.bsuir.academicauditsystemgateway.service;

import by.bsuir.academicauditsystemgateway.dto.UserDto;
import by.bsuir.academicauditsystemgateway.dto.mapper.UserMapper;
import by.bsuir.academicauditsystemgateway.entity.User;
import by.bsuir.academicauditsystemgateway.exception.UserNotFoundException;
import by.bsuir.academicauditsystemgateway.repository.UserRepository;
import by.bsuir.academicauditsystemgateway.exception.UserOperationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public User createUser(User user) {
        try {
            return userRepository.save(user);
        } catch (Exception e) {
            throw new UserOperationException("Error while creating user", e);
        }
    }

    public UserDto createUser(UserDto userDto) {
        return userMapper.toDto(createUser(userMapper.fromDto(userDto)));
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found with id " + id));
    }

    @Transactional
    public User updatePassword(Long id, String newPassword) {
        User user = findById(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    public User getByLogin(String login) {
        return userRepository.findByLogin(login).orElseThrow(() -> new UserNotFoundException("User not found with login " + login));
    }

    public boolean existsByLogin(String login) {
        return userRepository.findByLogin(login).isPresent();
    }

    public Page<User> getUsersWithPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable);
    }

    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }

        User user = userMapper.fromDto(userDto);
        user.setId(id);
        return userMapper.toDto(userRepository.save(user));
    }

    public UserDetailsService userDetailsService() {
        return this::getByLogin;
    }

    public UserDto deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }

        return userMapper.toDto(userRepository.deleteUserById(id));
    }

    public UserDto getDtoById(Long userId) {
        return userMapper.toDto(findById(userId));
    }
}
