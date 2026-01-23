package com.userservice.services;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.userservice.models.Users;
import com.userservice.repositories.UsersRepository;


@Service
@Transactional // All methods are transactional by default
public class UsersService implements UserDetailsService {

    @Autowired
    private UsersRepository usersRepository;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);


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
        if (usersRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already registered: " + user.getEmail());
        }

        //  Validation 2: Username must not already exist
        if (usersRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already taken: " + user.getUsername());
        }

        //  Validation 3: Password must be provided and meet minimum requirements
        if (user.getPasswordHash() == null || user.getPasswordHash().trim().isEmpty()) {
            throw new RuntimeException("Password is required");
        }
        
        if (user.getPasswordHash().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
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

        // Hash the PLAIN TEXT password before saving (produces 60+ char hash)
        String hashedPassword = encoder.encode(user.getPasswordHash());
        user.setPasswordHash(hashedPassword);

        //  If all validations pass, save user
        return usersRepository.save(user);
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<Users> userInfo = usersRepository.findActiveUserByUsername(username);

        if (userInfo.isEmpty()) {
            throw new UsernameNotFoundException("User not found with email: " + username);
        }

        return new UserPrincipal(userInfo.get());
    }


    // ==================== USER OPERATIONS ====================

    /**
     * Get user by ID
     *  Validates user exists, throws exception if not
     */
    public Users getUserById(Long userId) {
        return usersRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }

    /**
     * Get user by email
     *  Throws exception if not found
     */
    public Users getUserByEmailOrThrow(String email) {
        return usersRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    /**
     * Get user by username
     *  Throws exception if not found
     */
    public Users getUserByUsernameOrThrow(String username) {
        return usersRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
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
            if (usersRepository.existsByEmail(userDetails.getEmail())) {
                throw new RuntimeException("Email already in use: " + userDetails.getEmail());
            }
        }

        //  Update fields
        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        user.setPhoneNumber(userDetails.getPhoneNumber());
        user.setEmail(userDetails.getEmail());

        //  Save and return
        return usersRepository.save(user);
    }

    /**
     * Soft delete a user
     *  Marks user as deleted without removing from database
     * (Critical for financial/compliance reasons)
     */
    public void deleteUser(Long userId) {
        
        Users user = getUserById(userId);
        
        user.setIsActive(true);
        user.setIsDeleted(true);
        usersRepository.save(user);
    }

    /**
     * Deactivate user (temporary disable)
     *  User can be reactivated later
     */
    public void deactivateUser(Long userId) {
        Users user = getUserById(userId);
        user.setIsActive(false);
        usersRepository.save(user);
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
        usersRepository.save(user);
    }

    /**
     * Get all active, non-deleted users
     *  Useful for dashboard, reporting, authentication
     */
    public List<Users> getAllActiveUsers() {
        return usersRepository.findAllActiveUsers();
    }

    /**
     * Get count of active users
     *  Useful for admin dashboard metrics
     */
    public long getActiveUserCount() {
        return usersRepository.countActiveUsers();
    }
}
