package com.paymentservice.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "account_ledger")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountLedger {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accountLedgerId;
    
    // Reference to Payment in same service
    @ManyToOne(optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payments payment;
    
    // Reference to Account in OTHER service - store as value only!
    @Column(nullable = false)
    private Long accountId; 
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;
    
    @Column(nullable = false)
    private String description;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    public enum TransactionType {
        DEBIT,
        CREDIT
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        timestamp = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        timestamp = LocalDateTime.now();
    }
    
    // Constructor
    public AccountLedger(Payments payment, long accountId, TransactionType transactionType, 
                        BigDecimal amount, BigDecimal balanceAfter, String description) {
        this.payment = payment;
        this.accountId = accountId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
    }
}