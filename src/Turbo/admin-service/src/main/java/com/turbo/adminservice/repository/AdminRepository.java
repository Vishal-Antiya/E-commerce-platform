package com.turbo.adminservice.repository;

import com.turbo.adminservice.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    // Custom query methods
    Optional<Admin> findByUsername(String username); // Spring Data JPA magic
    Optional<Admin> findByEmail(String email);
}