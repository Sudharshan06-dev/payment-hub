package com.userservice.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.userservice.dto.AuthRequest;
import com.userservice.models.Users;
import com.userservice.services.OauthService;
import com.userservice.services.UsersService;

@RestController
@RequestMapping("/api/v1/auth")

public class LoginController {

    // Inject only Service layer
    @Autowired
    private UsersService usersService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private OauthService oauthService;

    @GetMapping
    public String hello() {
        return "Hellooo Spring Boot Makkale";
    }


    /**
     * POST /api/v1/register
     * Create a new user
     */
    @PostMapping("/register")
    public ResponseEntity<?> createUser(@RequestBody Users user) {
        try {
            // Service handles ALL validation and business logic
            Users savedUser = usersService.registerUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * POST /api/v1/login
     * Create a new user
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginByUsername(@RequestBody AuthRequest authRequest) {
        try {

            String bearerToken = null;
            
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));

            if (authentication.isAuthenticated()) {
                bearerToken = oauthService.generateToken(authRequest.getUsername());
            }
            else {
                throw new UsernameNotFoundException("Invalid user request!");
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(bearerToken);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    
}
