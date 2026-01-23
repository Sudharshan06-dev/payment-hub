package com.paymentservice.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import com.paymentservice.models.Payments;
import com.paymentservice.models.Payments.PaymentStatus;

@Repository
public interface PaymentRepository extends JpaRepository<Payments, Long> {
    
    // Basic CRUD is automatic from JpaRepository    
    // Custom query methods - define only what you NEED
    
    /**
     * Find all payments for a specific user
     */
    List<Payments> findByUserId(Long userId);
    
    /**
     * Find all payments for a specific bill
     */
    List<Payments> findByBillId(Long billId);

    /**
     * Find payments by account ID
     */
    List<Payments> findByAccountId(Long accountId);
    
    /**
     * Find all payments with a specific status
     */
    List<Payments> findByStatus(PaymentStatus status);
    
    /**
     * Find all payments for a user with specific status
     */
    List<Payments> findByUserIdAndStatus(Long userId, PaymentStatus status);
    
    /**
     * Custom query to find payments between dates
     * Spring generates SQL: SELECT * FROM payments WHERE payment_date >= ? AND payment_date <= ?
     */
    @Query("SELECT p FROM Payments p WHERE p.paymentDate >= :startDate AND p.paymentDate <= :endDate")
    List<Payments> findPaymentsBetweenDates(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Find all INITIATED payments (for batch processing)
     */
    List<Payments> findByStatusOrderByCreatedAtAsc(String status);
    
    /**
     * Count payments for a user
     */
    long countByUserId(Long userId);
    
    /**
     * Check if payment exists
     */
    boolean existsByPaymentId(Long paymentId);
}