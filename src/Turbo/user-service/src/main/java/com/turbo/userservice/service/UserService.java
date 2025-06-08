package com.turbo.userservice.service;

import com.turbo.userservice.model.User;
import com.turbo.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger; // Already imported
import org.slf4j.LoggerFactory; // Already imported
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class); // Logger instance already created

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    public User createUser(User user) {
        logger.info("Attempting to create user with username: {}", user.getUsername()); // Good use of logger
        // Hash the password before saving
        String encodedPassword = passwordEncoder.encode(user.getPasswordHash());
        user.setPasswordHash(encodedPassword);

        logger.debug("Encoded password for user {}: {}", user.getUsername(), encodedPassword); // More detailed debug log

        User savedUser = userRepository.save(user);
        logger.info("User created successfully with ID: {}", savedUser.getId());
        return savedUser;
    }

    public Optional<User> getUserById(Long id) {
        logger.debug("Fetching user by ID: {}", id);
        return userRepository.findById(id);
    }

    public List<User> getAllUsers() {
        logger.debug("Fetching all users");
        return userRepository.findAll();
    }

    public Optional<User> getUserByUsername(String username) {
        logger.debug("Fetching user by username: {}", username);
        return userRepository.findByUsername(username);
    }

    public User updateUser(Long id, User updatedUserDetails) {
        logger.info("Attempting to update user with ID: {}", id);
        return userRepository.findById(id).map(existingUser -> {
            existingUser.setUsername(updatedUserDetails.getUsername());
            existingUser.setEmail(updatedUserDetails.getEmail());
            existingUser.setFirstName(updatedUserDetails.getFirstName());
            existingUser.setLastName(updatedUserDetails.getLastName());
            if (updatedUserDetails.getPasswordHash() != null && !updatedUserDetails.getPasswordHash().isEmpty()) {
                // Only re-encode if a new password is provided
                existingUser.setPasswordHash(passwordEncoder.encode(updatedUserDetails.getPasswordHash()));
                logger.debug("Password updated for user ID: {}", id);
            }
            User savedUser = userRepository.save(existingUser);
            logger.info("User with ID: {} updated successfully.", id);
            return savedUser;
        }).orElseGet(() -> {
            logger.warn("User with ID: {} not found for update operation.", id);
            return null;
        });
    }

    public void deleteUser(Long id) {
        logger.info("Attempting to delete user with ID: {}", id);
        userRepository.deleteById(id);
        logger.info("User with ID: {} deleted successfully.", id);
    }
}