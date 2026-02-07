package com.paymentservice.models;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payments {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;
    
    // These are IDs from OTHER services - store as values only!
    @Column(nullable = false)
    private Long userId;  // From User Service (value, not foreign key)
    
    @Column(nullable = false)
    private Long billId;  // From Bill Service (value, not foreign key)
    
    @Column(nullable = true)
    private Long accountId;  // From Account Service (value, not foreign key)

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false, length = 3)
    private String currency = "USD";
    
    @Column(nullable = false)
    private LocalDateTime paymentDate;
    
    @Column(name = "payment_details", columnDefinition = "TEXT")  // or VARCHAR(2048) etc.
    private String paymentDetails;

    @Column(nullable = false)
    private String paymentMethod;  // CREDIT_CARD, BANK_TRANSFER, etc.
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;  // INITIATED, PROCESSING, SETTLED, FAILED, CANCELLED
    
    @Column(nullable = false)
    private String transactionReference;  // For tracking with banks
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // Define payment status enum
    public enum PaymentStatus {
        INITIATED,      // Payment just created
        PROCESSING,     // Being processed
        SETTLED,        // Successfully settled
        FAILED,         // Failed
        CANCELLED       // Cancelled by user
    }
    
    // Auto-set timestamps
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}