package com.turbo.adminservice.service;

import com.turbo.adminservice.model.Admin;
import com.turbo.adminservice.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Inject PasswordEncoder

    public Admin createUser(Admin admin) {
        admin.setPasswordHash(passwordEncoder.encode(admin.getPasswordHash()));
        admin.setRoles(List.of("ROLE_ADMIN"));  // Set the role here
        return adminRepository.save(admin);
    }


    public Optional<Admin> getUserById(Long id) {
        return adminRepository.findById(id);
    }

    public List<Admin> getAllUsers() {
        return adminRepository.findAll();
    }

    public Optional<Admin> getUserByUsername(String username) {
        return adminRepository.findByUsername(username);
    }

    public Admin updateUser(Long id, Admin updatedAdminDetails) {
        return adminRepository.findById(id).map(existingAdmin -> {
            existingAdmin.setUsername(updatedAdminDetails.getUsername());
            existingAdmin.setEmail(updatedAdminDetails.getEmail());
            existingAdmin.setFirstName(updatedAdminDetails.getFirstName());
            existingAdmin.setLastName(updatedAdminDetails.getLastName());
            if (updatedAdminDetails.getPasswordHash() != null && !updatedAdminDetails.getPasswordHash().isEmpty()) {
                existingAdmin.setPasswordHash(passwordEncoder.encode(updatedAdminDetails.getPasswordHash()));
            }
            return adminRepository.save(existingAdmin);
        }).orElse(null);
    }

    public void deleteUser(Long id) {
        adminRepository.deleteById(id);
    }
}