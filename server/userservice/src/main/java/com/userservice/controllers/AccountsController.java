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

import com.userservice.models.Accounts;
import com.userservice.models.Users;
import com.userservice.services.AccountsService;
import com.userservice.services.UsersService;


public class AccountsController {

    // Inject only Service layer
    @Autowired
    private AccountsService accountsService;

    /**
     * GET /api/v1/users/{userId}/accounts
     * Get all accounts for a user
     */
    @GetMapping("/{userId}/accounts")
    public ResponseEntity<?> getUserAccounts(@PathVariable Long userId) {
        try {
            List<Accounts> accounts = accountsService.getUserAccounts(userId);
            return ResponseEntity.ok(accounts);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * GET /api/v1/users/{userId}/accounts/active
     * Get only active accounts for a user
     */
    @GetMapping("/{userId}/accounts/active")
    public ResponseEntity<?> getUserActiveAccounts(@PathVariable Long userId) {
        try {
            List<Accounts> accounts = accountsService.getActiveAccounts(userId);
            return ResponseEntity.ok(accounts);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * GET /api/v1/users/{userId}/accounts/balance
     * Get total balance across all accounts
     */
    @GetMapping("/{userId}/accounts/balance")
    public ResponseEntity<?> getUserTotalBalance(@PathVariable Long userId) {
        try {
            BigDecimal totalBalance = accountsService.getTotalBalance(userId);
            return ResponseEntity.ok(totalBalance);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * GET /api/v1/users/{userId}/accounts/{accountNumber}
     * Get a specific account
     */
    @GetMapping("/{userId}/accounts/{accountNumber}")
    public ResponseEntity<?> getUserAccount(@PathVariable Long userId, @PathVariable String accountNumber) {
        try {
            // Service ensures security (user can only access own accounts)
            Accounts account = accountsService.getAccount(userId, accountNumber);
            return ResponseEntity.ok(account);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * POST /api/v1/users/{userId}/accounts
     * Create a new account for a user
     */
    @PostMapping("/{userId}/accounts")
    public ResponseEntity<?> createAccount(@PathVariable Long userId, @RequestBody Accounts account) {
        try {
            // Service handles validation and creation
            Accounts savedAccount = accountsService.createAccount(userId, account);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedAccount);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * GET /api/v1/users/stats/total-active
     * Get count of active users (admin dashboard)
     */
    @GetMapping("/stats/total-active")
    public ResponseEntity<?> getTotalActiveUsers() {
        long count = 0;
        //long count = accountsService.getActiveUserCount();
        return ResponseEntity.ok(count);
    }
    
}
