package com.userservice.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.userservice.models.Users;

/**
 * UserRepository - Data access layer for User entity
 * 
 * JpaRepository automatically provides:
 * - findById(Long id) - Get user by ID
 * - save(User user) - Create or update user
 * - delete(User user) - Delete user
 * - findAll() - Get all users
 * 
 * We add custom query methods for common operations
 */

@Repository
public interface UsersRepository extends JpaRepository <Users, Long> {

    /**
     * Find user by email address
     * Used for login and user lookup
     */
    Optional<Users> findByEmail(String email);

    /**
     * Find user by username
     * Used for authentication
     */
    Optional<Users> findByUsername(String username);


    /**
     * Check if email already exists (for registration)
     * More efficient than findByEmail when you only need boolean
     */
    boolean existsByEmail(String email);

    /**
     * Check if username already exists (for registration)
     */
    boolean existsByUsername(String username);

    /**
     * Find active users who are not deleted
     * Useful for dashboard, reporting
     */
    @Query("SELECT u FROM Users u WHERE u.isActive = true AND u.isDeleted = false")
    java.util.List<Users> findAllActiveUsers();


    @Query("SELECT * FROM Users where User.email = :email AND is_active = True AND is_deleted = false")
    Optional<Users> findActiveUserByEmail(@Param("email") String email);

    /**
     * Find user by username with custom query
     */
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.isDeleted = false")
    Optional<Users> findActiveUserByUsername(@Param("username") String username);

    /**
     * Count total active users
     * Useful for admin dashboard
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true AND u.isDeleted = false")
    long countActiveUsers();
}
