package com.paymentservice.controllers;

import java.math.BigDecimal;
import java.util.Optional;

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

import com.paymentservice.dto.AccountResponse;
import com.paymentservice.models.Accounts;
import com.paymentservice.services.AccountsService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/accounts")
@Slf4j
public class AccountsController {

    @Autowired
    private AccountsService accountsService;

    @GetMapping("/{userId}")
    public ResponseEntity<AccountResponse<Optional<Accounts>>> getAllAccounts(
            @PathVariable Long userId, 
            HttpServletRequest request) {
        
        log.info("GET request: Fetch all accounts for user: {}", userId);
        
        try {
            Optional<Accounts> accounts = accountsService.getAllAccountsByUserId(userId, request);
            
            return ResponseEntity.ok(
                AccountResponse.<Optional<Accounts>>builder()
                    .success(true)
                    .message("Accounts retrieved successfully")
                    .data(accounts)
                    .build()
            );
        } catch (RuntimeException e) {
            log.error("Error fetching accounts for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                AccountResponse.<Optional<Accounts>>builder()
                    .success(false)
                    .message(e.getMessage())
                    .data(null)
                    .build()
            );
        }
    }

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

    @PostMapping("/{userId}")
    public ResponseEntity<AccountResponse<Accounts>> createAccount(
            @PathVariable Long userId,
            @RequestBody Accounts account, 
            HttpServletRequest request) {
        
        log.info("POST request: Create account for user: {}", userId);
        
        try {
            Accounts createdAccount = accountsService.createAccount(userId, account, request);
            
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

    @PostMapping("/{userId}/{accountId}/validate")
    public ResponseEntity<AccountResponse<Boolean>> validatePayment(
            @PathVariable Long userId,
            @PathVariable Long accountId,
            @RequestBody BigDecimal amount,
            HttpServletRequest request) {
        
        log.info("POST request: Validate payment for user: {} account: {} amount: {}", userId, accountId, amount);
        
        try {
            accountsService.validateUserCanMakePayment(userId, accountId, amount, request);
            
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