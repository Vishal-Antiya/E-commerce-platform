package com.turbo.adminservice.service;

import com.turbo.adminservice.model.Admin;
import com.turbo.adminservice.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger; // Import Logger
import org.slf4j.LoggerFactory; // Import LoggerFactory

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class); // Add logger instance

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Admin createUser(Admin admin) {
        logger.info("Attempting to create new admin user: {}", admin.getUsername()); // Log creation attempt
        admin.setPasswordHash(passwordEncoder.encode(admin.getPasswordHash()));
        admin.setRoles(List.of("ROLE_ADMIN"));
        Admin savedAdmin = adminRepository.save(admin);
        logger.info("Admin user created successfully: {}", savedAdmin.getUsername()); // Log success
        return savedAdmin;
    }

    public Optional<Admin> getUserById(Long id) {
        logger.info("Fetching admin user by ID: {}", id); // Log fetch attempt
        Optional<Admin> admin = adminRepository.findById(id);
        if (admin.isPresent()) {
            logger.info("Found admin user with ID: {}", id);
        } else {
            logger.warn("Admin user with ID: {} not found.", id); // Log if not found
        }
        return admin;
    }

    public List<Admin> getAllUsers() {
        logger.info("Fetching all admin users."); // Log fetch attempt
        List<Admin> admins = adminRepository.findAll();
        logger.info("Fetched {} admin users.", admins.size()); // Log count
        return admins;
    }

    public Optional<Admin> getUserByUsername(String username) {
        logger.info("Fetching admin user by username: {}", username); // Log fetch attempt
        Optional<Admin> admin = adminRepository.findByUsername(username);
        if (admin.isPresent()) {
            logger.info("Found admin user with username: {}", username);
        } else {
            logger.warn("Admin user with username: {} not found.", username); // Log if not found
        }
        return admin;
    }

    public Admin updateUser(Long id, Admin updatedAdminDetails) {
        logger.info("Attempting to update admin user with ID: {}", id); // Log update attempt
        return adminRepository.findById(id).map(existingAdmin -> {
            existingAdmin.setUsername(updatedAdminDetails.getUsername());
            existingAdmin.setEmail(updatedAdminDetails.getEmail());
            existingAdmin.setFirstName(updatedAdminDetails.getFirstName());
            existingAdmin.setLastName(updatedAdminDetails.getLastName());
            if (updatedAdminDetails.getPasswordHash() != null && !updatedAdminDetails.getPasswordHash().isEmpty()) {
                existingAdmin.setPasswordHash(passwordEncoder.encode(updatedAdminDetails.getPasswordHash()));
            }
            Admin updatedAdmin = adminRepository.save(existingAdmin);
            logger.info("Admin user with ID: {} updated successfully.", id); // Log success
            return updatedAdmin;
        }).orElseGet(() -> {
            logger.warn("Admin user with ID: {} not found for update.", id); // Log if not found for update
            return null;
        });
    }

    public void deleteUser(Long id) {
        logger.info("Attempting to delete admin user with ID: {}", id); // Log delete attempt
        adminRepository.deleteById(id);
        logger.info("Admin user with ID: {} deleted successfully.", id); // Log success
    }
}