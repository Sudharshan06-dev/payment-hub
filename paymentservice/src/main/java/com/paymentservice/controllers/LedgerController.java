package com.paymentservice.controllers;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paymentservice.dto.LedgerResponse;
import com.paymentservice.models.AccountLedger;
import com.paymentservice.models.AccountLedger.TransactionType;
import com.paymentservice.services.AccountLedgerService;

@RestController
@RequestMapping("/api/v1/ledger")
public class LedgerController {

    @Autowired
    private AccountLedgerService accountLedgerService;
    
    /**
     * Record a new transaction
     * POST /api/v1/ledger
     * Body: {
     *   "paymentId": "xxx",
     *   "accountId": "xxx",
     *   "transactionType": "DEBIT",
     *   "amount": 50.00,
     *   "balanceAfter": 950.00,
     *   "description": "Payment for bill"
     * }
     */
    @PostMapping
    public ResponseEntity<LedgerResponse> recordTransaction(@RequestBody AccountLedger ledgerEntry) {
        try {
            AccountLedger recorded = accountLedgerService.recordTransaction(ledgerEntry);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                LedgerResponse.success("Transaction recorded successfully", recorded)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                LedgerResponse.error("Failed to record transaction: " + e.getMessage())
            );
        }
    }
    
    /**
     * Get all transactions for an account
     * GET /api/v1/ledger/{accountId}
     */
    @GetMapping("/{accountId}")
    public ResponseEntity<LedgerResponse> getAccountTransactions(@PathVariable Long accountId) {
        try {
            List<AccountLedger> transactions = accountLedgerService.getAccountTransactions(accountId);
            return ResponseEntity.ok(
                LedgerResponse.success("Transactions retrieved successfully", transactions)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                LedgerResponse.error("Failed to retrieve transactions: " + e.getMessage())
            );
        }
    }
    
    /**
     * Get debits or credits for an account
     * GET /api/v1/ledger/{accountId}/DEBIT
     * GET /api/v1/ledger/{accountId}/CREDIT
     */
    @GetMapping("/{accountId}/{transactionType}")
    public ResponseEntity<LedgerResponse> getTransactionsByType(
            @PathVariable Long accountId,
            @PathVariable TransactionType transactionType) {
        try {
            List<AccountLedger> transactions = accountLedgerService.getTransactionsByType(accountId, transactionType);
            return ResponseEntity.ok(
                LedgerResponse.success("Transactions retrieved successfully", transactions)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                LedgerResponse.error("Invalid transaction type: " + transactionType)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                LedgerResponse.error("Failed to retrieve transactions: " + e.getMessage())
            );
        }
    }

    /**
     * Get latest balance for account
     * GET /api/v1/ledger/{accountId}/balance
     */
    @GetMapping("/{accountId}/balance")
    public ResponseEntity<LedgerResponse> getLatestBalance(@PathVariable Long accountId) {
        try {
            AccountLedger latestEntry = accountLedgerService.getLatestBalance(accountId);
            return ResponseEntity.ok(
                LedgerResponse.success("Latest balance retrieved successfully", latestEntry)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                LedgerResponse.error("Failed to retrieve balance: " + e.getMessage())
            );
        }
    }

    /**
     * Get total debits for account
     * GET /api/v1/ledger/{accountId}/total-debits
     */
    @GetMapping("/{accountId}/total-debits")
    public ResponseEntity<LedgerResponse> getTotalDebits(@PathVariable Long accountId) {
        try {
            Double totalDebits = accountLedgerService.getTotalDebits(accountId);
            return ResponseEntity.ok(
                LedgerResponse.success("Total debits retrieved successfully", totalDebits)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                LedgerResponse.error("Failed to retrieve total debits: " + e.getMessage())
            );
        }
    }

    /**
     * Get total credits for account
     * GET /api/v1/ledger/{accountId}/total-credits
     */
    @GetMapping("/{accountId}/total-credits")
    public ResponseEntity<LedgerResponse> getTotalCredits(@PathVariable Long accountId) {
        try {
            Double totalCredits = accountLedgerService.getTotalCredits(accountId);
            return ResponseEntity.ok(
                LedgerResponse.success("Total credits retrieved successfully", totalCredits)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                LedgerResponse.error("Failed to retrieve total credits: " + e.getMessage())
            );
        }
    }
}