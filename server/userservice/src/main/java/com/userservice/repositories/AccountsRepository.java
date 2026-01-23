package com.userservice.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;  // ← FIX: Was .jdbc, should be .jpa
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.userservice.models.Accounts;
import com.userservice.models.Accounts.AccountStatus;
import java.math.BigDecimal;

@Repository
public interface AccountsRepository extends JpaRepository<Accounts, Long> {

    /**
     * Find all accounts for a specific user
     */
    List<Accounts> findByUser_UserId(Long userId);

    /**
     * Find account by account number (unique)
     */
    Optional<Accounts> findByAccountNumber(String accountNumber);

    /**
     * Check if account number already exists
     */
    boolean existsByAccountNumber(String accountNumber);

    /**
     * Find all active accounts for a user
     */
    @Query("SELECT a FROM Accounts a WHERE a.user.userId = :userId AND a.status = 'ACTIVE'")
    List<Accounts> findActiveAccountsByUserId(@Param("userId") Long userId);

    /**
     * Find specific account by user and account number
     */
    @Query("SELECT a FROM Accounts a WHERE a.user.userId = :userId AND a.accountNumber = :accountNumber")
    Optional<Accounts> findByUserIdAndAccountNumber(
        @Param("userId") Long userId, 
        @Param("accountNumber") String accountNumber
    );

    /**
     * Count total accounts for a user
     */
    @Query("SELECT COUNT(a) FROM Accounts a WHERE a.user.userId = :userId")
    long countAccountsByUserId(@Param("userId") Long userId);

    /**
     * Find accounts by status (for filtering)
     */
    @Query("SELECT a FROM Accounts a WHERE a.user.userId = :userId AND a.status = :status")
    List<Accounts> findByUserIdAndStatus(
        @Param("userId") Long userId,
        @Param("status") AccountStatus status
    );

    /**
     * Calculate total balance across all user's accounts
     */
    @Query("SELECT COALESCE(SUM(a.balance), 0) FROM Accounts a WHERE a.user.userId = :userId AND a.status = 'ACTIVE'")
    BigDecimal getTotalBalanceByUserId(@Param("userId") Long userId);
}