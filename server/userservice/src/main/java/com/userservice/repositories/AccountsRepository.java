package com.userservice.repositories;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.userservice.models.Accounts;
import com.userservice.models.Accounts.AccountStatus;

/**
 * Repository for Accounts entity
 * Provides database access methods for account operations
 * All queries include security checks (isDeleted = false by default)
 */
@Repository
public interface AccountsRepository extends JpaRepository<Accounts, Long> {

    /**
     * Find all accounts for a specific user
     * Returns list of accounts (user can have multiple accounts)
     * 
     * Query: SELECT * FROM accounts WHERE user_id = ? AND is_deleted = false
     * 
     * @param userId - User ID
     * @return List of accounts for the user
     */
    @Query("SELECT a FROM Accounts a WHERE a.user.userId = :userId AND a.isDeleted = false")
    List<Accounts> findByUser_UserId(@Param("userId") Long userId);

    /**
     * Find a specific account by ID and verify it belongs to the user
     * Security: Ensures user can only access their own accounts
     * 
     * Query: SELECT * FROM accounts WHERE account_id = ? AND user_id = ? AND is_deleted = false
     * 
     * @param accountId - Account ID
     * @param userId - User ID
     * @return Optional containing the account if found and belongs to user
     */
    @Query("SELECT a FROM Accounts a WHERE a.accountId = :accountId AND a.user.userId = :userId AND a.isDeleted = false")
    Optional<Accounts> findByAccountIdAndUser_UserId(
        @Param("accountId") Long accountId,
        @Param("userId") Long userId
    );

    /**
     * Find account by account number (unique identifier)
     * Useful for manual lookups
     * 
     * Query: SELECT * FROM accounts WHERE account_number = ? AND is_deleted = false
     * 
     * @param accountNumber - Account number
     * @return Optional containing the account if found
     */
    @Query("SELECT a FROM Accounts a WHERE a.accountNumber = :accountNumber AND a.isDeleted = false")
    Optional<Accounts> findByAccountNumber(@Param("accountNumber") String accountNumber);

    /**
     * Find account by account number AND user ID
     * Double verification for security
     * 
     * Query: SELECT * FROM accounts WHERE account_number = ? AND user_id = ? AND is_deleted = false
     * 
     * @param accountNumber - Account number
     * @param userId - User ID
     * @return Optional containing the account if found and belongs to user
     */
    @Query("SELECT a FROM Accounts a WHERE a.accountNumber = :accountNumber AND a.user.userId = :userId AND a.isDeleted = false")
    Optional<Accounts> findByAccountNumberAndUser_UserId(
        @Param("accountNumber") String accountNumber,
        @Param("userId") Long userId
    );

    /**
     * Find all ACTIVE accounts for a user (excludes INACTIVE, FROZEN, CLOSED)
     * Used for payment processing and dashboard
     * 
     * Query: SELECT * FROM accounts WHERE user_id = ? AND status = 'ACTIVE' AND is_deleted = false
     * 
     * @param userId - User ID
     * @return List of active accounts
     */
    @Query("SELECT a FROM Accounts a WHERE a.user.userId = :userId AND a.status = 'ACTIVE' AND a.isDeleted = false")
    List<Accounts> findActiveAccountsByUserId(@Param("userId") Long userId);

    /**
     * Find all accounts for a user filtered by status
     * Useful for dashboard filtering (show frozen, closed accounts separately)
     * 
     * Query: SELECT * FROM accounts WHERE user_id = ? AND status = ? AND is_deleted = false
     * 
     * @param userId - User ID
     * @param status - Account status to filter by
     * @return List of accounts with specified status
     */
    @Query("SELECT a FROM Accounts a WHERE a.user.userId = :userId AND a.status = :status AND a.isDeleted = false")
    List<Accounts> findByUserIdAndStatus(
        @Param("userId") Long userId,
        @Param("status") AccountStatus status
    );

    // ============== COUNT & EXISTENCE CHECKS ==============

    /**
     * Check if account number already exists (for unique validation)
     * Used during account creation to prevent duplicate account numbers
     * 
     * Query: SELECT EXISTS(SELECT 1 FROM accounts WHERE account_number = ?)
     * 
     * @param accountNumber - Account number to check
     * @return true if account number exists, false otherwise
     */
    boolean existsByAccountNumber(String accountNumber);

    /**
     * Check if account exists for a specific user (ownership verification)
     * Security: Ensures account belongs to the user
     * 
     * Query: SELECT EXISTS(SELECT 1 FROM accounts WHERE account_id = ? AND user_id = ?)
     * 
     * @param accountId - Account ID
     * @param userId - User ID
     * @return true if account exists and belongs to user
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Accounts a WHERE a.accountId = :accountId AND a.user.userId = :userId AND a.isDeleted = false")
    boolean existsByAccountIdAndUser_UserId(
        @Param("accountId") Long accountId,
        @Param("userId") Long userId
    );

    /**
     * Count total number of accounts for a user
     * Used for validation (max 10 accounts per user)
     * 
     * Query: SELECT COUNT(*) FROM accounts WHERE user_id = ? AND is_deleted = false
     * 
     * @param userId - User ID
     * @return Total number of accounts
     */
    @Query("SELECT COUNT(a) FROM Accounts a WHERE a.user.userId = :userId AND a.isDeleted = false")
    long countByUser_UserId(@Param("userId") Long userId);

    /**
     * Alternative count method (Spring Data native support)
     * Same as countByUser_UserId but uses different naming convention
     * 
     * @param userId - User ID
     * @return Total number of accounts
     */
    @Query("SELECT COUNT(a) FROM Accounts a WHERE a.user.userId = :userId AND a.isDeleted = false")
    long countAccountsByUserId(@Param("userId") Long userId);

    // ============== BALANCE OPERATIONS ==============

    /**
     * Calculate total balance across all ACTIVE accounts for a user
     * Used for dashboard showing net worth
     * Returns 0 if no active accounts exist
     * 
     * Query: SELECT SUM(balance) FROM accounts WHERE user_id = ? AND status = 'ACTIVE' AND is_deleted = false
     * 
     * @param userId - User ID
     * @return Total balance (0 if no accounts)
     */
    @Query("SELECT COALESCE(SUM(a.balance), 0) FROM Accounts a WHERE a.user.userId = :userId AND a.status = 'ACTIVE' AND a.isDeleted = false")
    BigDecimal getTotalBalanceByUserId(@Param("userId") Long userId);

    /**
     * Get maximum balance among all user's accounts
     * Useful for analytics
     * 
     * Query: SELECT MAX(balance) FROM accounts WHERE user_id = ? AND is_deleted = false
     * 
     * @param userId - User ID
     * @return Maximum balance wrapped in Optional
     */
    @Query("SELECT MAX(a.balance) FROM Accounts a WHERE a.user.userId = :userId AND a.isDeleted = false")
    Optional<BigDecimal> getMaxBalanceByUserId(@Param("userId") Long userId);

    /**
     * Get minimum balance among all user's accounts
     * Useful for analytics
     * 
     * Query: SELECT MIN(balance) FROM accounts WHERE user_id = ? AND is_deleted = false
     * 
     * @param userId - User ID
     * @return Minimum balance wrapped in Optional
     */
    @Query("SELECT MIN(a.balance) FROM Accounts a WHERE a.user.userId = :userId AND a.isDeleted = false")
    Optional<BigDecimal> getMinBalanceByUserId(@Param("userId") Long userId);

    // ============== SOFT DELETE OPERATIONS ==============

    /**
     * Find all deleted accounts (for admin/audit purposes)
     * 
     * Query: SELECT * FROM accounts WHERE user_id = ? AND is_deleted = true
     * 
     * @param userId - User ID
     * @return List of deleted accounts
     */
    @Query("SELECT a FROM Accounts a WHERE a.user.userId = :userId AND a.isDeleted = true")
    List<Accounts> findDeletedAccountsByUserId(@Param("userId") Long userId);

    /**
     * Count deleted accounts for a user
     * 
     * Query: SELECT COUNT(*) FROM accounts WHERE user_id = ? AND is_deleted = true
     * 
     * @param userId - User ID
     * @return Count of deleted accounts
     */
    @Query("SELECT COUNT(a) FROM Accounts a WHERE a.user.userId = :userId AND a.isDeleted = true")
    long countDeletedAccountsByUserId(@Param("userId") Long userId);
}