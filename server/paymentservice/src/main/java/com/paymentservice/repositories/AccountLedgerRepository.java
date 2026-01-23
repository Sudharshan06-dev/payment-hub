package com.paymentservice.repositories;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.paymentservice.models.AccountLedger;
import com.paymentservice.models.AccountLedger.TransactionType;

@Repository
public interface AccountLedgerRepository extends JpaRepository<AccountLedger, Long> {
    
    // ===== BASIC QUERY METHODS =====
    
    /**
     * Get all ledger entries for an account
     */
    List<AccountLedger> findByAccountId(Long accountId);
    
    /**
     * Get all transactions for an account with pagination
     */
    Page<AccountLedger> findByAccountId(Long accountId, Pageable pageable);
    
    /**
     * Get all debits (money going out)
     */
    List<AccountLedger> findByTransactionType(TransactionType transactionType);
    
    /**
     * Get all transactions for a specific payment
     */
    List<AccountLedger> findByPaymentId(Long paymentId);

    /**
     * Get all transactions for a specific payment
     */
    List<AccountLedger> findByAccountIdAndTransactionType(Long accountId, TransactionType type);
    
    /**
     * Check if account has any ledger entries
     */
    boolean existsByAccountId(Long accountId);
    
    // ===== CUSTOM QUERIES =====
    
    /**
     * Get ledger entries for account created between dates (for statements)
     */
    @Query("""
        SELECT l FROM account_ledger l 
        WHERE l.accountId = :accountId 
        AND l.createdAt >= :startDate 
        AND l.createdAt < :endDate 
        ORDER BY l.createdAt DESC
    """)
    Page<AccountLedger> findAccountTransactionsByDateRange(
        @Param("accountId") Long accountId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
    
    /**
     * Get only debit transactions for an account (money going out)
     */
    @Query("""
        SELECT l FROM account_ledger l 
        WHERE l.accountId = :accountId 
        AND l.transactionType = 'DEBIT'
        ORDER BY l.createdAt DESC
    """)
    List<AccountLedger> findDebitsForAccount(@Param("accountId") Long accountId);
    
    /**
     * Get only credit transactions for an account (money coming in)
     */
    @Query("""
        SELECT l FROM account_ledger l 
        WHERE l.accountId = :accountId 
        AND l.transactionType = 'CREDIT'
        ORDER BY l.createdAt DESC
    """)
    List<AccountLedger> findCreditsForAccount(@Param("accountId") Long accountId);
    
    /**
     * Get total debits for account (money paid out)
     */
    @Query(value = """
        SELECT SUM(l.amount) FROM account_ledger l 
        WHERE l.account_id = :accountId 
        AND l.transaction_type = 'DEBIT'
    """, nativeQuery = true)
    Double getTotalDebitsForAccount(@Param("accountId") Long accountId);
    
    /**
     * Get total credits for account (money received)
     */
    @Query(value = """
        SELECT SUM(l.amount) FROM account_ledger l 
        WHERE l.account_id = :accountId 
        AND l.transaction_type = 'CREDIT'
    """, nativeQuery = true)
    Double getTotalCreditsForAccount(@Param("accountId") Long accountId);
    
    /**
     * Get latest balance for an account
     */
    @Query("""
        SELECT l.balanceAfter FROM account_ledger l 
        WHERE l.accountId = :accountId 
        ORDER BY l.createdAt DESC 
        LIMIT 1
    """)
    Double getLatestBalanceForAccount(@Param("accountId") Long accountId);
    
    /**
     * Get ledger entries created today (for daily reports)
     */
    @Query("SELECT l FROM account_ledger l WHERE DATE(l.createdAt) = CURRENT_DATE")
    List<AccountLedger> findTodayTransactions();

    /**
     * Get latest ledger entry for an account (to get current balance)
     */
    @Query("SELECT al FROM account_ledger al WHERE al.accountId = :accountId ORDER BY al.createdAt DESC LIMIT 1")
    AccountLedger getLatestLedgerEntry(@Param("accountId") Long accountId);
    
    /**
     * Get transaction count for account
     */
    long countByAccountId(Long accountId);
}