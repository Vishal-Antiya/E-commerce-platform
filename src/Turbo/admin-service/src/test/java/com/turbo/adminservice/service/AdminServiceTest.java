package com.turbo.adminservice.service;

import com.turbo.adminservice.model.Admin;
import com.turbo.adminservice.repository.AdminRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AdminServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminService adminService;

    private Admin admin;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize an admin object
        admin = new Admin();
        admin.setId(1L);
        admin.setUsername("adminuser");
        admin.setPasswordHash("password");
        admin.setEmail("admin@example.com");
    }

    @Test
    void testCreateAdmin() {
        // Arrange
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(adminRepository.save(any(Admin.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Admin createdAdmin = adminService.createUser(admin);

        // Assert
        ArgumentCaptor<Admin> adminCaptor = ArgumentCaptor.forClass(Admin.class);
        verify(adminRepository).save(adminCaptor.capture());
        Admin savedAdmin = adminCaptor.getValue();

        assertEquals("adminuser", savedAdmin.getUsername());
        assertEquals("encodedPassword", savedAdmin.getPasswordHash());
        assertEquals("admin@example.com", savedAdmin.getEmail());
    }

    @Test
    void testGetAdminById_AdminExists() {
        // Arrange
        when(adminRepository.findById(1L)).thenReturn(Optional.of(admin));

        // Act
        Optional<Admin> foundAdmin = adminService.getUserById(1L);

        // Assert
        assertTrue(foundAdmin.isPresent());
        assertEquals(admin, foundAdmin.get());
        verify(adminRepository, times(1)).findById(1L);
    }

    @Test
    void testGetAdminById_AdminNotFound() {
        // Arrange
        when(adminRepository.findById(2L)).thenReturn(Optional.empty());

        // Act
        Optional<Admin> foundAdmin = adminService.getUserById(2L);

        // Assert
        assertTrue(foundAdmin.isEmpty());
        verify(adminRepository, times(1)).findById(2L);
    }

    @Test
    void testUpdateAdmin_AdminExists() {
        // Arrange
        Admin updatedAdminDetails = new Admin();
        updatedAdminDetails.setUsername("updatedAdmin");
        updatedAdminDetails.setEmail("updated@example.com");

        when(adminRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(adminRepository.save(any(Admin.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Admin updatedAdmin = adminService.updateUser(1L, updatedAdminDetails);

        // Assert
        assertNotNull(updatedAdmin);
        assertEquals("updatedAdmin", updatedAdmin.getUsername());
        assertEquals("updated@example.com", updatedAdmin.getEmail());
        verify(adminRepository, times(1)).findById(1L);
        verify(adminRepository, times(1)).save(any(Admin.class));
    }

    @Test
    void testUpdateAdmin_AdminNotFound() {
        // Arrange
        Admin updatedAdminDetails = new Admin();
        when(adminRepository.findById(2L)).thenReturn(Optional.empty());

        // Act
        Admin updatedAdmin = adminService.updateUser(2L, updatedAdminDetails);

        // Assert
        assertNull(updatedAdmin);
        verify(adminRepository, times(1)).findById(2L);
        verify(adminRepository, never()).save(any(Admin.class));
    }

    @Test
    void testDeleteAdmin() {
        // Act
        adminService.deleteUser(1L);

        // Assert
        verify(adminRepository, times(1)).deleteById(1L);
    }
}