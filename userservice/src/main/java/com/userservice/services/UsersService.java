package com.userservice.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.userservice.models.Users;
import com.userservice.repositories.UsersRepository;

/**
 * UserService - Business Logic Layer
 * 
 *  CORRECT APPROACH:
 * - Service is THICK - contains ALL business logic
 * - Service performs ALL validation
 * - Service handles transactions
 * - Service calls Repository for database operations
 * - Controller NEVER calls Repository directly
 * 
 * Flow: Controller → Service → Repository → Database
 */
@Service
@Transactional // All methods are transactional by default
public class UsersService {

    @Autowired
    private UsersRepository userRepository;

    // ==================== USER OPERATIONS ====================

    /**
     * Get user by ID
     *  Validates user exists, throws exception if not
     */
    public Users getUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }

    /**
     * Get user by email
     *  Throws exception if not found
     */
    public Users getUserByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    /**
     * Get user by username
     *  Throws exception if not found
     */
    public Users getUserByUsernameOrThrow(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }

    /**
     * Register a new user
     *  Business Logic:
     *    1. Validates email doesn't exist
     *    2. Validates username doesn't exist
     *    3. Validates password is hashed (bcrypt)
     *    4. Saves user to database
     */
    public Users registerUser(Users user) {

        //  Validation 1: Email must not already exist
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already registered: " + user.getEmail());
        }

        //  Validation 2: Username must not already exist
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already taken: " + user.getUsername());
        }

        //  Validation 3: Password must be bcrypt hashed (minimum 60 characters for bcrypt)
        if (user.getPasswordHash() == null || user.getPasswordHash().length() < 60) {
            throw new RuntimeException(
                "Password must be hashed with bcrypt (minimum 60 chars). " +
                "Never store plain text passwords!"
            );
        }

        //  Validation 4: First/Last name required
        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            throw new RuntimeException("First name is required");
        }

        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            throw new RuntimeException("Last name is required");
        }

        //  Validation 5: Email format basic check
        if (!user.getEmail().contains("@") || !user.getEmail().contains(".")) {
            throw new RuntimeException("Invalid email format");
        }

        //  If all validations pass, save user
        return userRepository.save(user);
    }

    /**
     * Update user information
     *  Validates user exists before updating
     */
    public Users updateUser(Long userId, Users userDetails) {
        //  Check user exists
        Users user = getUserById(userId);

        //  Validate new email doesn't conflict (if email changed)
        if (!user.getEmail().equals(userDetails.getEmail())) {
            if (userRepository.existsByEmail(userDetails.getEmail())) {
                throw new RuntimeException("Email already in use: " + userDetails.getEmail());
            }
        }

        //  Update fields
        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        user.setPhoneNumber(userDetails.getPhoneNumber());
        user.setEmail(userDetails.getEmail());

        //  Save and return
        return userRepository.save(user);
    }

    /**
     * Soft delete a user
     *  Marks user as deleted without removing from database
     * (Critical for financial/compliance reasons)
     */
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    /**
     * Deactivate user (temporary disable)
     *  User can be reactivated later
     */
    public void deactivateUser(Long userId) {
        Users user = getUserById(userId);
        user.setIsActive(false);
        userRepository.save(user);
    }

    /**
     * Reactivate user
     *  Can only activate if not deleted
     */
    public void activateUser(Long userId) {
        Users user = getUserById(userId);
        
        //  Can't activate a deleted user
        if (user.getIsDeleted()) {
            throw new RuntimeException("Cannot activate a deleted user");
        }
        
        user.setIsActive(true);
        userRepository.save(user);
    }

    /**
     * Get all active, non-deleted users
     *  Useful for dashboard, reporting, authentication
     */
    public List<Users> getAllActiveUsers() {
        return userRepository.findAllActiveUsers();
    }

    /**
     * Get count of active users
     *  Useful for admin dashboard metrics
     */
    public long getActiveUserCount() {
        return userRepository.countActiveUsers();
    }
}
