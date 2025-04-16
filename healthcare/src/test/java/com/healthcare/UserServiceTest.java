package com.healthcare;

import com.healthcare.model.User;
import com.healthcare.repository.UserRepository;
import com.healthcare.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerUser_Success() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setFirstName("John");
        user.setLastName("Doe");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        User result = userService.registerUser(user);

        // Assert
        assertNotNull(result);
        assertEquals("encodedPassword", result.getPassword());
        assertEquals(User.Role.PATIENT, result.getRole());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void registerUser_EmailAlreadyExists() {
        // Arrange
        User user = new User();
        user.setEmail("existing@example.com");
        user.setPassword("password");

        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.registerUser(user);
        });

        assertEquals("Email already in use", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void findByEmail_Success() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        // Act
        Optional<User> result = userService.findByEmail("test@example.com");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("test@example.com", result.get().getEmail());
    }

    @Test
    void findByEmail_NotFound() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findByEmail("nonexistent@example.com");

        // Assert
        assertFalse(result.isPresent());
    }
}