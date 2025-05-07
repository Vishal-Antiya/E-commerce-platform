package com.turbo.userservice.repository;

import com.turbo.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> { // User and Long

    // Custom query methods
    Optional<User> findByUsername(String username); // Spring Data JPA magic
    Optional<User> findByEmail(String email);

    // Example of a custom query with @Query (if you need more control)
    // @Query("SELECT u FROM User u WHERE u.username = :username AND u.email = :email")
    // User findByUsernameAndEmail(@Param("username") String username, @Param("email") String email);
}