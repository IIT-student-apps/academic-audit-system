package by.bsuir.academicauditsystemgateway.service;

import by.bsuir.academicauditsystemgateway.dto.UserDto;
import by.bsuir.academicauditsystemgateway.dto.mapper.UserMapper;
import by.bsuir.academicauditsystemgateway.entity.User;
import by.bsuir.academicauditsystemgateway.exception.UserNotFoundException;
import by.bsuir.academicauditsystemgateway.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        user.setLogin("testuser");
        user.setPassword("password123");
    }

    @Test
    void testCreateUser() {
        when(userRepository.save(any())).thenReturn(user);
        User savedUser = userService.createUser(user);
        assertEquals(savedUser.getLogin(), "testuser");
    }

    @Test
    void testFindByIdUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.findById(1L));
    }

    @Test
    void testGetByLogin() {
        when(userRepository.findByLogin("testuser")).thenReturn(Optional.of(user));
        User foundUser = userService.getByLogin("testuser");
        assertEquals(foundUser.getLogin(), "testuser");
    }

    @Test
    void testUpdatePassword() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedPassword");

        User updatedUser = userService.updatePassword(1L, "newPassword");
        assertEquals(updatedUser.getPassword(), "encodedPassword");
    }
}
