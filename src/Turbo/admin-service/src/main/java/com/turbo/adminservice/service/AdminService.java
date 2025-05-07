package com.turbo.adminservice.service;

import com.turbo.adminservice.model.Admin;
import com.turbo.adminservice.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    public Admin createUser(Admin admin) {
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

    public void deleteUser(Long id) {
        adminRepository.deleteById(id);
    }
}
