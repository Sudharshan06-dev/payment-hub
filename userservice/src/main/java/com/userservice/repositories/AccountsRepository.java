package com.userservice.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.userservice.models.Accounts;
import com.userservice.models.Accounts.AccountStatus;


//links this interface to your Accounts class
//matches the type of the field annotated with @Id in your entity class

@Repository
public interface AccountsRepository extends JpaRepository <Accounts, Long> {

    /**
     * Find all accounts for a specific user
     * Used when customer wants to see their accounts
     */
    List<Accounts> findByUser_UserId(Long userId);

    /**
     * Find account by account number (unique)
     * Used for account lookup
     */
    Optional<Accounts> findByAccountNumber(String accountNumber);

    /**
     * Check if account number already exists
     * Used for account creation validation
     */
    boolean existsByAccountNumber(String accountNumber);

    /**
     * Find all active accounts for a user
     * Excludes closed/frozen accounts
     */
    @Query("SELECT a FROM Account a WHERE a.user.userId = :userId AND a.status = 'ACTIVE'")
    List<Accounts> findActiveAccountsByUserId(@Param("userId") Long userId);

    /**
     * Find specific account by user and account number
     * Ensures user can only access their own accounts
     */
    @Query("SELECT a FROM Account a WHERE a.user.userId = :userId AND a.accountNumber = :accountNumber")
    Optional<Accounts> findByUserIdAndAccountNumber(
        @Param("userId") Long userId, 
        @Param("accountNumber") String accountNumber
    );

    /**
     * Count total accounts for a user
     * Useful for validation (max 10 accounts per user, etc.)
     */
    @Query("SELECT COUNT(a) FROM Account a WHERE a.user.userId = :userId")
    long countAccountsByUserId(@Param("userId") Long userId);

    /**
     * Find accounts by status (for filtering)
     * Used for admin dashboard, reporting
     */
    @Query("SELECT a FROM Account a WHERE a.user.userId = :userId AND a.status = :status")
    List<Accounts> findByUserIdAndStatus(
        @Param("userId") Long userId,
        @Param("status") AccountStatus status
    );

    /**
     * Calculate total balance across all user's accounts
     * Useful for dashboard showing total net worth
     */
    @Query("SELECT COALESCE(SUM(a.balance), 0) FROM Account a WHERE a.user.userId = :userId AND a.status = 'ACTIVE'")
    java.math.BigDecimal getTotalBalanceByUserId(@Param("userId") Long userId);
    
}
