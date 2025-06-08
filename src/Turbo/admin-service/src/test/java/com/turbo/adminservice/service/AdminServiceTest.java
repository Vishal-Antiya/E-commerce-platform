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

import java.util.Arrays;
import java.util.List;
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
        admin.setFirstName("John"); // Added for more comprehensive testing
        admin.setLastName("Doe");   // Added for more comprehensive testing
    }

    @Test
    void testCreateAdmin() {
        // Arrange
        Admin adminToCreate = new Admin();
        adminToCreate.setUsername("newadmin");
        adminToCreate.setPasswordHash("newpassword");
        adminToCreate.setEmail("newadmin@example.com");
        adminToCreate.setFirstName("Jane");
        adminToCreate.setLastName("Smith");

        when(passwordEncoder.encode("newpassword")).thenReturn("encodedNewPassword");
        when(adminRepository.save(any(Admin.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Admin createdAdmin = adminService.createUser(adminToCreate);

        // Assert
        ArgumentCaptor<Admin> adminCaptor = ArgumentCaptor.forClass(Admin.class);
        verify(adminRepository).save(adminCaptor.capture());
        Admin savedAdmin = adminCaptor.getValue();

        assertEquals("newadmin", savedAdmin.getUsername()); //cite: 1
        assertEquals("encodedNewPassword", savedAdmin.getPasswordHash()); //cite: 1
        assertEquals("newadmin@example.com", savedAdmin.getEmail()); //cite: 1
        assertEquals("Jane", savedAdmin.getFirstName()); //cite: 1
        assertEquals("Smith", savedAdmin.getLastName()); //cite: 1
        assertNotNull(savedAdmin.getRoles()); // Check if roles are set
        assertFalse(savedAdmin.getRoles().isEmpty());
        assertTrue(savedAdmin.getRoles().contains("ROLE_ADMIN"));
    }

    @Test
    void testGetAdminById_AdminExists() {
        // Arrange
        when(adminRepository.findById(1L)).thenReturn(Optional.of(admin)); //cite: 1

        // Act
        Optional<Admin> foundAdmin = adminService.getUserById(1L); //cite: 1

        // Assert
        assertTrue(foundAdmin.isPresent()); //cite: 1
        assertEquals(admin, foundAdmin.get()); //cite: 1
        verify(adminRepository, times(1)).findById(1L); //cite: 1
    }

    @Test
    void testGetAdminById_AdminNotFound() {
        // Arrange
        when(adminRepository.findById(2L)).thenReturn(Optional.empty()); //cite: 1

        // Act
        Optional<Admin> foundAdmin = adminService.getUserById(2L); //cite: 1

        // Assert
        assertTrue(foundAdmin.isEmpty()); //cite: 1
        verify(adminRepository, times(1)).findById(2L); //cite: 1
    }

    @Test
    void testGetAllUsers() {
        // Arrange
        Admin admin1 = new Admin("admin1", "admin1@example.com", "pass1", "Admin", "One");
        Admin admin2 = new Admin("admin2", "admin2@example.com", "pass2", "Admin", "Two");
        List<Admin> adminList = Arrays.asList(admin1, admin2);
        when(adminRepository.findAll()).thenReturn(adminList); //cite: 1

        // Act
        List<Admin> foundAdmins = adminService.getAllUsers(); //cite: 1

        // Assert
        assertNotNull(foundAdmins); //cite: 1
        assertEquals(2, foundAdmins.size()); //cite: 1
        assertTrue(foundAdmins.contains(admin1)); //cite: 1
        assertTrue(foundAdmins.contains(admin2)); //cite: 1
        verify(adminRepository, times(1)).findAll(); //cite: 1
    }

    @Test
    void testGetUserByUsername_AdminExists() {
        // Arrange
        String username = "adminuser";
        when(adminRepository.findByUsername(username)).thenReturn(Optional.of(admin)); //cite: 1

        // Act
        Optional<Admin> foundAdmin = adminService.getUserByUsername(username); //cite: 1

        // Assert
        assertTrue(foundAdmin.isPresent()); //cite: 1
        assertEquals(username, foundAdmin.get().getUsername()); //cite: 1
        verify(adminRepository, times(1)).findByUsername(username); //cite: 1
    }

    @Test
    void testGetUserByUsername_AdminNotFound() {
        // Arrange
        String username = "nonexistentuser";
        when(adminRepository.findByUsername(username)).thenReturn(Optional.empty()); //cite: 1

        // Act
        Optional<Admin> foundAdmin = adminService.getUserByUsername(username); //cite: 1

        // Assert
        assertTrue(foundAdmin.isEmpty()); //cite: 1
        verify(adminRepository, times(1)).findByUsername(username); //cite: 1
    }

    @Test
    void testUpdateAdmin_AdminExists() {
        // Arrange
        Admin updatedAdminDetails = new Admin();
        updatedAdminDetails.setUsername("updatedAdmin");
        updatedAdminDetails.setEmail("updated@example.com");
        updatedAdminDetails.setFirstName("UpdatedJohn");
        updatedAdminDetails.setLastName("UpdatedDoe");
        updatedAdminDetails.setPasswordHash("newEncodedPassword");

        when(adminRepository.findById(1L)).thenReturn(Optional.of(admin)); //cite: 1
        when(passwordEncoder.encode("newEncodedPassword")).thenReturn("hashedNewEncodedPassword");
        when(adminRepository.save(any(Admin.class))).thenAnswer(invocation -> invocation.getArgument(0)); //cite: 1

        // Act
        Admin updatedAdmin = adminService.updateUser(1L, updatedAdminDetails); //cite: 1

        // Assert
        assertNotNull(updatedAdmin); //cite: 1
        assertEquals("updatedAdmin", updatedAdmin.getUsername()); //cite: 1
        assertEquals("updated@example.com", updatedAdmin.getEmail()); //cite: 1
        assertEquals("UpdatedJohn", updatedAdmin.getFirstName()); //cite: 1
        assertEquals("UpdatedDoe", updatedAdmin.getLastName()); //cite: 1
        assertEquals("hashedNewEncodedPassword", updatedAdmin.getPasswordHash()); // Verify password is re-encoded if provided
        verify(adminRepository, times(1)).findById(1L); //cite: 1
        verify(adminRepository, times(1)).save(any(Admin.class)); //cite: 1
    }

    @Test
    void testUpdateAdmin_AdminNotFound() {
        // Arrange
        Admin updatedAdminDetails = new Admin();
        when(adminRepository.findById(2L)).thenReturn(Optional.empty()); //cite: 1

        // Act
        Admin updatedAdmin = adminService.updateUser(2L, updatedAdminDetails); //cite: 1

        // Assert
        assertNull(updatedAdmin); //cite: 1
        verify(adminRepository, times(1)).findById(2L); //cite: 1
        verify(adminRepository, never()).save(any(Admin.class)); //cite: 1
    }

    @Test
    void testDeleteAdmin() {
        // Act
        adminService.deleteUser(1L); //cite: 1

        // Assert
        verify(adminRepository, times(1)).deleteById(1L); //cite: 1
    }
}