package com.turbo.userservice.service;

import com.turbo.userservice.model.User;
import com.turbo.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class UserServiceTest {

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
    void testCreateUser() {
        // Arrange
        User userToCreate = new User();
        userToCreate.setUsername("testuser");
        userToCreate.setPasswordHash("password");
        userToCreate.setEmail("test@example.com");
        userToCreate.setFirstName("John"); // Added fields
        userToCreate.setLastName("Doe");   // Added fields

        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User createdUser = userService.createUser(userToCreate);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals("testuser", savedUser.getUsername()); //cite: 1
        assertEquals("encodedPassword", savedUser.getPasswordHash()); //cite: 1
        assertEquals("test@example.com", savedUser.getEmail()); //cite: 1
        assertEquals("John", savedUser.getFirstName()); //cite: 1
        assertEquals("Doe", savedUser.getLastName()); //cite: 1
    }

    @Test
    void testGetUserById() {
        // Arrange
        Long userId = 1L;
        User user = new User("testuser", "test@example.com", "password", "John", "Doe");
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user)); //cite: 1

        // Act
        Optional<User> foundUser = userService.getUserById(userId); //cite: 1

        // Assert
        assertTrue(foundUser.isPresent()); //cite: 1
        assertEquals(userId, foundUser.get().getId()); //cite: 1
        assertEquals("testuser", foundUser.get().getUsername()); //cite: 1
        verify(userRepository, times(1)).findById(userId); //cite: 1
    }

    @Test
    void testGetAllUsers() {
        // Arrange
        User user1 = new User("user1", "user1@example.com", "pass1", "First1", "Last1");
        User user2 = new User("user2", "user2@example.com", "pass2", "First2", "Last2");
        List<User> users = Arrays.asList(user1, user2);
        when(userRepository.findAll()).thenReturn(users); //cite: 1

        // Act
        List<User> allUsers = userService.getAllUsers(); //cite: 1

        // Assert
        assertEquals(2, allUsers.size()); //cite: 1
        assertTrue(allUsers.contains(user1)); //cite: 1
        assertTrue(allUsers.contains(user2)); //cite: 1
        verify(userRepository, times(1)).findAll(); //cite: 1
    }

    @Test
    void testGetUserByUsername() {
        // Arrange
        String username = "testuser";
        User user = new User(username, "test@example.com", "password", "John", "Doe");
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user)); //cite: 1

        // Act
        Optional<User> foundUser = userService.getUserByUsername(username); //cite: 1

        // Assert
        assertTrue(foundUser.isPresent()); //cite: 1
        assertEquals(username, foundUser.get().getUsername()); //cite: 1
        verify(userRepository, times(1)).findByUsername(username); //cite: 1
    }

    @Test
    void testDeleteUser() {
        // Arrange
        Long userId = 1L;
        doNothing().when(userRepository).deleteById(userId); //cite: 1

        // Act
        userService.deleteUser(userId); //cite: 1

        // Assert
        verify(userRepository, times(1)).deleteById(userId); //cite: 1
    }
}