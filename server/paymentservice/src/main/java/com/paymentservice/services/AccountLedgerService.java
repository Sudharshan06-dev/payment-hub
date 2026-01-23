package com.paymentservice.services;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paymentservice.models.AccountLedger;
import com.paymentservice.models.AccountLedger.TransactionType;
import com.paymentservice.repositories.AccountLedgerRepository;

@Service
@Transactional
public class AccountLedgerService {
    
    @Autowired
    private AccountLedgerRepository ledgerRepository;
    
    /**
     * Record a new transaction in the ledger
     */
    public AccountLedger recordTransaction(AccountLedger ledgerEntry) {
        
        // Validate
        if (ledgerEntry.getPayment() == null) {
            throw new RuntimeException("Payment is required for ledger entry");
        }
        
        if (ledgerEntry.getAccountId() == null) {
            throw new RuntimeException("Account ID is required");
        }
        
        if (ledgerEntry.getAmount() == null || ledgerEntry.getAmount().signum() <= 0) {
            throw new RuntimeException("Amount must be greater than 0");
        }
        
        if (ledgerEntry.getTransactionType() == null) {
            throw new RuntimeException("Transaction type is required");
        }
        
        return ledgerRepository.save(ledgerEntry);
    }
    
    /**
     * Get all transactions for an account
     */
    public List<AccountLedger> getAccountTransactions(Long accountId) {
        if (accountId == null) {
            throw new RuntimeException("Account ID is required");
        }
        
        return ledgerRepository.findByAccountId(accountId);
    }
    
    /**
     * Get transactions of specific type (DEBIT or CREDIT)
     */
    public List<AccountLedger> getTransactionsByType(Long accountId, TransactionType type) {
        if (accountId == null) {
            throw new RuntimeException("Account ID is required");
        }
        
        if (type == null) {
            throw new RuntimeException("Transaction type is required");
        }
        
        return ledgerRepository.findByAccountIdAndTransactionType(accountId, type);
    }
    
    /**
     * Get latest balance for an account
     */
    public AccountLedger getLatestBalance(Long accountId) {
        if (accountId == null) {
            throw new RuntimeException("Account ID is required");
        }
        
        AccountLedger latest = ledgerRepository.getLatestLedgerEntry(accountId);
        
        if (latest == null) {
            throw new RuntimeException("No ledger entries found for account: " + accountId);
        }
        
        return latest;
    }
    
    /**
     * Get total debit amount for an account
     */
    public Double getTotalDebits(Long accountId) {
        if (accountId == null) {
            throw new RuntimeException("Account ID is required");
        }
        
        Double total = ledgerRepository.getTotalDebitsForAccount(accountId);
        return total != null ? total : 0.0;
    }
    
    /**
     * Get total credit amount for an account
     */
    public Double getTotalCredits(Long accountId) {
        if (accountId == null) {
            throw new RuntimeException("Account ID is required");
        }
        
        Double total = ledgerRepository.getTotalCreditsForAccount(accountId);
        return total != null ? total : 0.0;
    }
    
    /**
     * Get current balance (credits - debits)
     */
    public Double getCurrentBalance(Long accountId) {
        Double credits = this.getTotalCredits(accountId);
        Double debits = this.getTotalDebits(accountId);
        return credits - debits;
    }
}