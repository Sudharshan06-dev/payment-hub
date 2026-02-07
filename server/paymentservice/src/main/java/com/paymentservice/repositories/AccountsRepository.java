package com.paymentservice.repositories;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.paymentservice.models.Accounts;
import com.paymentservice.models.Accounts.AccountStatus;

@Repository
public interface AccountsRepository extends JpaRepository<Accounts, Long> {

    @Query("SELECT a FROM Accounts a WHERE a.userId = :userId AND a.isDeleted = false")
    Optional<Accounts> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT a FROM Accounts a WHERE a.accountId = :accountId AND a.userId = :userId AND a.isDeleted = false")
    Optional<Accounts> findByAccountIdAndUserId(
        @Param("accountId") Long accountId,
        @Param("userId") Long userId
    );

    @Query("SELECT a FROM Accounts a WHERE a.accountNumber = :accountNumber AND a.isDeleted = false")
    Optional<Accounts> findByAccountNumber(@Param("accountNumber") String accountNumber);

    @Query("SELECT a FROM Accounts a WHERE a.accountNumber = :accountNumber AND a.userId = :userId AND a.isDeleted = false")
    Optional<Accounts> findByAccountNumberAndUserId(
        @Param("accountNumber") String accountNumber,
        @Param("userId") Long userId
    );

    @Query("SELECT a FROM Accounts a WHERE a.userId = :userId AND a.status = 'ACTIVE' AND a.isDeleted = false")
    Optional<Accounts> findActiveAccountsByUserId(@Param("userId") Long userId);

    @Query("SELECT a FROM Accounts a WHERE a.userId = :userId AND a.status = :status AND a.isDeleted = false")
    Optional<Accounts> findByUserIdAndStatus(
        @Param("userId") Long userId,
        @Param("status") AccountStatus status
    );

    boolean existsByAccountNumber(String accountNumber);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Accounts a WHERE a.accountId = :accountId AND a.userId = :userId AND a.isDeleted = false")
    boolean existsByAccountIdAndUserId(
        @Param("accountId") Long accountId,
        @Param("userId") Long userId
    );

    @Query("SELECT COUNT(a) FROM Accounts a WHERE a.userId = :userId AND a.isDeleted = false")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(a.balance), 0) FROM Accounts a WHERE a.userId = :userId AND a.status = 'ACTIVE' AND a.isDeleted = false")
    BigDecimal getTotalBalanceByUserId(@Param("userId") Long userId);

    @Query("SELECT a FROM Accounts a WHERE a.userId = :userId AND a.isDeleted = true")
    Optional<Accounts> findDeletedAccountsByUserId(@Param("userId") Long userId);
}