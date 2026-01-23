package com.userservice.controllers;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.userservice.models.Users;
import com.userservice.services.UsersService;

/**
 * UserController - REST API Layer
 * 
 * CORRECT APPROACH:
 * - Controller is THIN - only handles HTTP
 * - Controller ONLY calls Service methods
 * - NO business logic in controller
 * - NO database queries in controller
 * - NO validation in controller
 * 
 * Separation of Concerns:
 * - Controller: HTTP stuff
 * - Service: Business logic, validation, transactions
 * - Repository: Database operations
 */
@RestController
@RequestMapping("/api/v1/users")
public class UsersController {

    // Inject only Service layer
    @Autowired
    private UsersService usersService;

    // ==================== USER ENDPOINTS ====================

    /**
     * GET /api/v1/users/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUser(@PathVariable Long userId) {
        try {
            Users user = usersService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * GET /api/v1/users/email/{email}
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        try {
            Users user = usersService.getUserByEmailOrThrow(email);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * GET /api/v1/users/username/{username}
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        try {
            Users user = usersService.getUserByUsernameOrThrow(username);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * GET /api/v1/users
     * Get all active users
     */
    @GetMapping
    public ResponseEntity<List<Users>> getAllActiveUsers() {
        List<Users> users = usersService.getAllActiveUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * PUT /api/v1/users/{userId}
     * Update an existing user
     */
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody Users userDetails) {
        try {
            // Service handles validation and update logic
            Users updatedUser = usersService.updateUser(userId, userDetails);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * DELETE /api/v1/users/{userId}
     * Soft delete a user
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            // Service handles soft delete logic
            usersService.deleteUser(userId);
            return ResponseEntity.ok("Users soft deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * POST /api/v1/users/{userId}/activate
     * Reactivate a deactivated user
     */
    @PostMapping("/{userId}/activate")
    public ResponseEntity<?> activateUser(@PathVariable Long userId) {
        try {
            usersService.activateUser(userId);
            return ResponseEntity.ok("Users activated successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * POST /api/v1/users/{userId}/deactivate
     * Temporarily deactivate a user
     */
    @PostMapping("/{userId}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable Long userId) {
        try {
            usersService.deactivateUser(userId);
            return ResponseEntity.ok("Users deactivated successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // ==================== ACCOUNT ENDPOINTS ====================
}