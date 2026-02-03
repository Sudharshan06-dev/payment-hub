package com.billservice.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.billservice.models.Bills;
import com.billservice.models.Bills.BillStatus;

@Repository
public interface BillsRepository extends JpaRepository<Bills, Long> {

    /**
     * Get all bills for a specific user (excluding deleted bills)
     */
    @Query("SELECT b FROM Bills b WHERE b.userId = :userId AND b.isDeleted = false ORDER BY b.dueDate ASC")
    List<Bills> findAllByUserIdNotDeleted(@Param("userId") Long userId);

    /**
     * Get bills by user ID and status
     */
    @Query("SELECT b FROM Bills b WHERE b.userId = :userId AND b.billStatus = :status AND b.isDeleted = false")
    List<Bills> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") BillStatus status);

    /**
     * Get overdue bills for a user
     */
    @Query("SELECT b FROM Bills b WHERE b.userId = :userId AND b.billStatus = :status AND b.dueDate < CURRENT_TIMESTAMP AND b.isDeleted = false")
    List<Bills> findOverdueBills(@Param("userId") Long userId, @Param("status") BillStatus status);

    /**
     * Get bills due within specified days
     */
    @Query("SELECT b FROM Bills b WHERE b.userId = :userId AND b.dueDate <= :dueDate AND b.billStatus != com.billservice.models.Bills.BillStatus.PAID AND b.isDeleted = false")
    List<Bills> findBillsDueWithin(@Param("userId") Long userId, @Param("dueDate") LocalDateTime dueDate);

    /**
     * Get a specific bill by ID and user ID
     */
    @Query("SELECT b FROM Bills b WHERE b.billId = :billId AND b.userId = :userId AND b.isDeleted = false")
    Optional<Bills> findByIdAndUserId(@Param("billId") Long billId, @Param("userId") Long userId);

    /**
     * Get bill count by status for a user
     */
    @Query("SELECT COUNT(b) FROM Bills b WHERE b.userId = :userId AND b.billStatus = :status AND b.isDeleted = false")
    Long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") BillStatus status);

    /**
     * Check if bill exists for user
     */
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Bills b WHERE b.billId = :billId AND b.userId = :userId AND b.isDeleted = false")
    Boolean existsByIdAndUserId(@Param("billId") Long billId, @Param("userId") Long userId);

    /**
     * Find bills by biller name (for searching)
     */
    @Query("SELECT b FROM Bills b WHERE b.userId = :userId AND b.billerName LIKE %:billerName% AND b.isDeleted = false")
    List<Bills> findByUserIdAndBillerNameContaining(@Param("userId") Long userId, @Param("billerName") String billerName);

    /**
     * Get total amount owed by user (all non-paid bills)
     */
    @Query("SELECT COALESCE(SUM(b.amount), 0.0) FROM Bills b WHERE b.userId = :userId AND b.billStatus IN (com.billservice.models.Bills.BillStatus.PENDING, com.billservice.models.Bills.BillStatus.OVERDUE) AND b.isDeleted = false")
    Double getTotalAmountOwed(@Param("userId") Long userId);

    /**
     * Delete bills (soft delete)
     */
    @Query("UPDATE Bills b SET b.isDeleted = true, b.updatedAt = CURRENT_TIMESTAMP WHERE b.billId = :billId AND b.userId = :userId")
    void softDeleteBill(@Param("billId") Long billId, @Param("userId") Long userId);
}