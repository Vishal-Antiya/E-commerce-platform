package com.turbo.userservice.service;

import com.turbo.userservice.model.User;
import com.turbo.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateUser() {
        // Arrange
        User userToSave = new User("testuser", "test@example.com", "password", "Test", "User");
        User savedUser = new User("testuser", "test@example.com", "password", "Test", "User");
        savedUser.setId(1L);

        when(userRepository.save(userToSave)).thenReturn(savedUser);

        // Act
        User actualUser = userService.createUser(userToSave);

        // Assert
        assertNotNull(actualUser.getId());
        assertEquals(savedUser.getUsername(), actualUser.getUsername());
        assertEquals(savedUser.getEmail(), actualUser.getEmail());
        verify(userRepository, times(1)).save(userToSave);
    }

//    @Test
//    public void testGetUserById() {
//        // Arrange
//        User user = new User("testuser", "test@example.com", "password", "Test", "User");
//        user.setId(1L);
//
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//
//        // Act
//        Optional<User> foundUser = userService.getUserById(1L);
//
//        // Assert
//        assertTrue(foundUser.isPresent());
//        assertEquals(user.getUsername(), foundUser.get().getUsername());
//        verify(userRepository, times(1)).findById(1L);
//    }
//
//    @Test
//    public void testGetUserByIdNotFound() {
//        // Arrange
//        when(userRepository.findById(1L)).thenReturn(Optional.empty());
//
//        // Act
//        Optional<User> foundUser = userService.getUserById(1L);
//
//        // Assert
//        assertFalse(foundUser.isPresent());
//        verify(userRepository, times(1)).findById(1L);
//    }
//
//    @Test
//    public void testGetAllUsers() {
//        // Arrange
//        List<User> userList = new ArrayList<>();
//        userList.add(new User("user1", "user1@example.com", "pass", "User", "One"));
//        userList.add(new User("user2", "user2@example.com", "pass", "User", "Two"));
//
//        when(userRepository.findAll()).thenReturn(userList);
//
//        // Act
//        List<User> allUsers = userService.getAllUsers();
//
//        // Assert
//        assertEquals(2, allUsers.size());
//        assertEquals("user1", allUsers.get(0).getUsername());
//        verify(userRepository, times(1)).findAll();
//    }
//
//    @Test
//    public void testGetUserByUsername() {
//        // Arrange
//        User user = new User("testuser", "test@example.com", "password", "Test", "User");
//        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
//
//        // Act
//        Optional<User> foundUser = userService.getUserByUsername("testuser");
//
//        // Assert
//        assertTrue(foundUser.isPresent());
//        assertEquals("testuser", foundUser.get().getUsername());
//        verify(userRepository, times(1)).findByUsername("testuser");
//    }
//
//    @Test
//    public void testGetUserByUsernameNotFound() {
//        // Arrange
//        when(userRepository.findByUsername("nonexistentuser")).thenReturn(Optional.empty());
//
//        // Act
//        Optional<User> foundUser = userService.getUserByUsername("nonexistentuser");
//
//        // Assert
//        assertFalse(foundUser.isPresent());
//        verify(userRepository, times(1)).findByUsername("nonexistentuser");
//    }
//
//    @Test
//    public void testDeleteUser() {
//        // Act
//        userService.deleteUser(1L);
//
//        // Assert
//        verify(userRepository, times(1)).deleteById(1L);
//    }
}