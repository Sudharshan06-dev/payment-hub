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

import com.userservice.dto.AccountResponse;
import com.userservice.models.Accounts;
import com.userservice.services.AccountsService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/accounts")
@Slf4j
public class AccountsController {

    @Autowired
    private AccountsService accountsService;

    /**
     * GET /api/v1/users/{userId}/accounts
     * Get all accounts for a user
     * 
     * @param userId - User ID
     * @return List of accounts with success response
     */
    @GetMapping("/{userId}")
    public ResponseEntity<AccountResponse<List<Accounts>>> getAllAccounts(@PathVariable Long userId) {
        log.info("GET request: Fetch all accounts for user: {}", userId);
        
        try {
            List<Accounts> accounts = accountsService.getAllAccountsByUserId(userId);
            
            return ResponseEntity.ok(
                AccountResponse.<List<Accounts>>builder()
                    .success(true)
                    .message("Accounts retrieved successfully")
                    .data(accounts)
                    .build()
            );
        } catch (RuntimeException e) {
            log.error("Error fetching accounts for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                AccountResponse.<List<Accounts>>builder()
                    .success(false)
                    .message(e.getMessage())
                    .data(null)
                    .build()
            );
        }
    }

    /**
     * GET /api/v1/users/{userId}/accounts/{accountId}
     * Get a specific account
     * 
     * @param userId - User ID
     * @param accountId - Account ID
     * @return Single account with success response
     */
    @GetMapping("/{userId}/{accountId}")
    public ResponseEntity<AccountResponse<Accounts>> getAccountById(
            @PathVariable Long userId,
            @PathVariable Long accountId) {
        
        log.info("GET request: Fetch account {} for user: {}", accountId, userId);
        
        try {
            Accounts account = accountsService.getAccountById(userId, accountId);
            
            return ResponseEntity.ok(
                AccountResponse.<Accounts>builder()
                    .success(true)
                    .message("Account retrieved successfully")
                    .data(account)
                    .build()
            );
        } catch (RuntimeException e) {
            log.error("Error fetching account {} for user: {}", accountId, userId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                AccountResponse.<Accounts>builder()
                    .success(false)
                    .message(e.getMessage())
                    .data(null)
                    .build()
            );
        }
    }

    /**
     * GET /api/v1/users/{userId}/accounts/active
     * Get only active accounts
     * 
     * @param userId - User ID
     * @return List of active accounts
     */
    @GetMapping("/{userId}/active")
    public ResponseEntity<AccountResponse<List<Accounts>>> getActiveAccounts(@PathVariable Long userId) {
        log.info("GET request: Fetch active accounts for user: {}", userId);
        
        try {
            List<Accounts> accounts = accountsService.getActiveAccounts(userId);
            
            return ResponseEntity.ok(
                AccountResponse.<List<Accounts>>builder()
                    .success(true)
                    .message("Active accounts retrieved successfully")
                    .data(accounts)
                    .build()
            );
        } catch (RuntimeException e) {
            log.error("Error fetching active accounts for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                AccountResponse.<List<Accounts>>builder()
                    .success(false)
                    .message(e.getMessage())
                    .data(null)
                    .build()
            );
        }
    }

    /**
     * GET /api/v1/users/{userId}/accounts/primary
     * Get primary (first active) account
     * 
     * @param userId - User ID
     * @return Primary account
     */
    @GetMapping("/{userId}/primary")
    public ResponseEntity<AccountResponse<Accounts>> getPrimaryAccount(@PathVariable Long userId) {
        log.info("GET request: Fetch primary account for user: {}", userId);
        
        try {
            var account = accountsService.getPrimaryAccount(userId);
            
            if (account.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    AccountResponse.<Accounts>builder()
                        .success(false)
                        .message("No active account found")
                        .data(null)
                        .build()
                );
            }
            
            return ResponseEntity.ok(
                AccountResponse.<Accounts>builder()
                    .success(true)
                    .message("Primary account retrieved successfully")
                    .data(account.get())
                    .build()
            );
        } catch (RuntimeException e) {
            log.error("Error fetching primary account for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                AccountResponse.<Accounts>builder()
                    .success(false)
                    .message(e.getMessage())
                    .data(null)
                    .build()
            );
        }
    }

    /**
     * GET /api/v1/users/{userId}/accounts/balance
     * Get total balance across all accounts
     * 
     * @param userId - User ID
     * @return Total balance
     */
    @GetMapping("/{userId}/balance")
    public ResponseEntity<AccountResponse<BigDecimal>> getTotalBalance(@PathVariable Long userId) {
        log.info("GET request: Fetch total balance for user: {}", userId);
        
        try {
            BigDecimal totalBalance = accountsService.getTotalBalance(userId);
            
            return ResponseEntity.ok(
                AccountResponse.<BigDecimal>builder()
                    .success(true)
                    .message("Total balance retrieved successfully")
                    .data(totalBalance)
                    .build()
            );
        } catch (RuntimeException e) {
            log.error("Error fetching balance for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                AccountResponse.<BigDecimal>builder()
                    .success(false)
                    .message(e.getMessage())
                    .data(null)
                    .build()
            );
        }
    }

    /**
     * POST /api/v1/users/{userId}/accounts
     * Create a new account
     * 
     * @param userId - User ID
     * @param account - Account details to create
     * @return Created account with HTTP 201 CREATED
     */
    @PostMapping("/{userId}")
    public ResponseEntity<AccountResponse<Accounts>> createAccount(
            @PathVariable Long userId,
            @RequestBody Accounts account) {
        
        log.info("POST request: Create account for user: {}", userId);
        
        try {
            Accounts createdAccount = accountsService.createAccount(userId, account);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(
                AccountResponse.<Accounts>builder()
                    .success(true)
                    .message("Account created successfully")
                    .data(createdAccount)
                    .build()
            );
        } catch (RuntimeException e) {
            log.error("Error creating account for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                AccountResponse.<Accounts>builder()
                    .success(false)
                    .message(e.getMessage())
                    .data(null)
                    .build()
            );
        }
    }

    /**
     * PUT /api/v1/users/{userId}/accounts/{accountId}
     * Update an existing account
     * 
     * @param userId - User ID
     * @param accountId - Account ID to update
     * @param accountDetails - New account details
     * @return Updated account
     */
    @PutMapping("/{userId}/{accountId}")
    public ResponseEntity<AccountResponse<Accounts>> updateAccount(
            @PathVariable Long userId,
            @PathVariable Long accountId,
            @RequestBody Accounts accountDetails) {
        
        log.info("PUT request: Update account {} for user: {}", accountId, userId);
        
        try {
            Accounts updatedAccount = accountsService.updateAccount(userId, accountId, accountDetails);
            
            return ResponseEntity.ok(
                AccountResponse.<Accounts>builder()
                    .success(true)
                    .message("Account updated successfully")
                    .data(updatedAccount)
                    .build()
            );
        } catch (RuntimeException e) {
            log.error("Error updating account {} for user: {}", accountId, userId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                AccountResponse.<Accounts>builder()
                    .success(false)
                    .message(e.getMessage())
                    .data(null)
                    .build()
            );
        }
    }

    /**
     * DELETE /api/v1/users/{userId}/accounts/{accountId}
     * Soft delete an account
     * 
     * @param userId - User ID
     * @param accountId - Account ID to delete
     * @return Success message
     */
    @DeleteMapping("/{userId}/{accountId}")
    public ResponseEntity<AccountResponse<Void>> deleteAccount(
            @PathVariable Long userId,
            @PathVariable Long accountId) {
        
        log.info("DELETE request: Delete account {} for user: {}", accountId, userId);
        
        try {
            accountsService.deleteAccount(userId, accountId);
            
            return ResponseEntity.ok(
                AccountResponse.<Void>builder()
                    .success(true)
                    .message("Account deleted successfully")
                    .data(null)
                    .build()
            );
        } catch (RuntimeException e) {
            log.error("Error deleting account {} for user: {}", accountId, userId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                AccountResponse.<Void>builder()
                    .success(false)
                    .message(e.getMessage())
                    .data(null)
                    .build()
            );
        }
    }

    /**
     * GET /api/v1/users/{userId}/accounts/count
     * Get total number of accounts for user
     * 
     * @param userId - User ID
     * @return Account count
     */
    @GetMapping("/{userId}/count")
    public ResponseEntity<AccountResponse<Long>> getAccountCount(@PathVariable Long userId) {
        log.info("GET request: Fetch account count for user: {}", userId);
        
        try {
            long count = accountsService.countUserAccounts(userId);
            
            return ResponseEntity.ok(
                AccountResponse.<Long>builder()
                    .success(true)
                    .message("Account count retrieved successfully")
                    .data(count)
                    .build()
            );
        } catch (RuntimeException e) {
            log.error("Error fetching account count for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                AccountResponse.<Long>builder()
                    .success(false)
                    .message(e.getMessage())
                    .data(null)
                    .build()
            );
        }
    }

    /**
     * POST /api/v1/users/{userId}/accounts/{accountId}/validate
     * Validate if user can make a payment with this account
     * 
     * @param userId - User ID
     * @param accountId - Account ID
     * @param amount - Payment amount
     * @return Validation result
     */
    @PostMapping("/{userId}/{accountId}/validate")
    public ResponseEntity<AccountResponse<Boolean>> validatePayment(
            @PathVariable Long userId,
            @PathVariable Long accountId,
            @RequestBody BigDecimal amount) {
        
        log.info("POST request: Validate payment for user: {} account: {} amount: {}", userId, accountId, amount);
        
        try {
            accountsService.validateUserCanMakePayment(userId, accountId, amount);
            
            return ResponseEntity.ok(
                AccountResponse.<Boolean>builder()
                    .success(true)
                    .message("Account is valid for payment")
                    .data(true)
                    .build()
            );
        } catch (RuntimeException e) {
            log.warn("Payment validation failed for user: {} account: {}: {}", userId, accountId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                AccountResponse.<Boolean>builder()
                    .success(false)
                    .message(e.getMessage())
                    .data(false)
                    .build()
            );
        }
    }
}