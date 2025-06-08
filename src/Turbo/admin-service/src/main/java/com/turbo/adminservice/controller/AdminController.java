package com.turbo.adminservice.controller;

import com.turbo.adminservice.model.Admin;
import com.turbo.adminservice.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger; // Import Logger
import org.slf4j.LoggerFactory; // Import LoggerFactory

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class); // Add logger instance

    @Autowired
    private AdminService adminService;

    @PostMapping("/create")
    public ResponseEntity<Admin> createUser(@RequestBody Admin admin) {
        logger.info("Received request to create admin user: {}", admin.getUsername()); // Log request
        Admin createdUser = adminService.createUser(admin);
        logger.info("Admin user created via controller: {}", createdUser.getUsername()); // Log success
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Admin> getUserById(@PathVariable Long id) {
        logger.info("Received request to get admin user by ID: {}", id); // Log request
        Optional<Admin> admin = adminService.getUserById(id);
        return admin.map(value -> {
                    logger.info("Successfully retrieved admin user by ID: {}", id); // Log success
                    return new ResponseEntity<>(value, HttpStatus.OK);
                })
                .orElseGet(() -> {
                    logger.warn("Admin user with ID: {} not found for GET request.", id); // Log if not found
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                });
    }

    @GetMapping("/allUsers")
    public ResponseEntity<List<Admin>> getAllUsers() {
        logger.info("Received request to get all admin users."); // Log request
        List<Admin> admin = adminService.getAllUsers();
        logger.info("Successfully retrieved all admin users (count: {}).", admin.size()); // Log success
        return new ResponseEntity<>(admin, HttpStatus.OK);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<Admin> getUserByUsername(@PathVariable String username) {
        logger.info("Received request to get admin user by username: {}", username); // Log request
        Optional<Admin> admin = adminService.getUserByUsername(username);
        return admin.map(value -> {
                    logger.info("Successfully retrieved admin user by username: {}", username); // Log success
                    return new ResponseEntity<>(value, HttpStatus.OK);
                })
                .orElseGet(() -> {
                    logger.warn("Admin user with username: {} not found for GET request.", username); // Log if not found
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                });
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        logger.info("Received request to delete admin user by ID: {}", id); // Log request
        adminService.deleteUser(id);
        logger.info("Admin user with ID: {} deleted successfully via controller.", id); // Log success
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}