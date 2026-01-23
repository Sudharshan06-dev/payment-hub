package com.paymentservice.services;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paymentservice.models.Payments;
import com.paymentservice.models.Payments.PaymentStatus;
import com.paymentservice.repositories.PaymentRepository;
import com.paymentservice.dto.CreatePaymentRequest;

@Service
@Transactional
public class PaymentsService {
    
    @Autowired
    private PaymentRepository paymentRepository;
        
    /**
     * Create a new payment
     */
    public Payments createPayment(CreatePaymentRequest request) {
        
        // Validate input
        if (request.getAmount() == null || request.getAmount().signum() <= 0) {
            throw new RuntimeException("Amount must be greater than 0");
        }
        
        if (request.getAccountId() == null) {
            throw new RuntimeException("Account ID is required");
        }
        
        if (request.getUserId() == null) {
            throw new RuntimeException("User ID is required");
        }
        
        if (request.getBillId() == null) {
            throw new RuntimeException("Bill ID is required");
        }
        
        // Create payment
        Payments payment = new Payments();
        payment.setUserId(request.getUserId());
        payment.setBillId(request.getBillId());
        payment.setAccountId(request.getAccountId());
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        payment.setPaymentDate(java.time.LocalDateTime.now());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus(PaymentStatus.INITIATED);
        payment.setTransactionReference("TXN-" + UUID.randomUUID());
        
        Payments savedPayment = paymentRepository.save(payment);
        
        // TODO: Publish PaymentInitiated event here
        // eventPublisher.publishEvent(new PaymentInitiatedEvent(savedPayment));
        
        return savedPayment;
    }
    
    /**
     * Get payment by ID
     */
    public Payments getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));
    }
    
    /**
     * Get all payments for a user (paginated)
     */
    public Page<Payments> getAllPaymentsByUser(Long userId, Pageable pageable) {
        return paymentRepository.findAll(pageable); // TODO: Implement custom pagination by userId
    }
    
    /**
     * Get payments by status
     */
    public List<Payments> getPaymentsByStatus(PaymentStatus status) {
        try {
            return paymentRepository.findByStatus(status);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid payment status: " + status);
        }
    }
    
    /**
     * Get user's payments with specific status
     */
    public List<Payments> getUserPaymentsByStatus(Long userId, PaymentStatus status) {
        try {
            return paymentRepository.findByUserIdAndStatus(userId, status);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid payment status: " + status);
        }
    }
    
    /**
     * Update payment status
     */
    public Payments updatePaymentStatus(Long paymentId, String newStatus) {
        Payments payment = getPaymentById(paymentId);
        
        try {
            PaymentStatus status = PaymentStatus.valueOf(newStatus.toUpperCase());
            payment.setStatus(status);
            
            Payments updatedPayment = paymentRepository.save(payment);
            
            // TODO: Publish PaymentStatusChanged event
            //KAFKA FOR FRAUD DETECTION AND NOTIFICATION SERVICE -> IMPORTANT
            
            return updatedPayment;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid payment status: " + newStatus);
        }
    }
    
    /**
     * Delete a payment (only if INITIATED)
     */
    public void deletePayment(Long paymentId) {
        
        if (paymentId == null) {
            throw new RuntimeException("Payment ID cannot be null");
        }
        
        Payments payment = getPaymentById(paymentId);
        
        if (!payment.getStatus().equals(PaymentStatus.INITIATED)) {
            throw new RuntimeException("Can only delete INITIATED payments");
        }
        
        paymentRepository.deleteById(paymentId);
    }
    
    /**
     * Get all INITIATED payments (for batch processing)
     */
    public List<Payments> getPendingPaymentsForBatch() {
        return paymentRepository.findByStatusOrderByCreatedAtAsc("INITIATED");
    }
    
    /**
     * Batch update payment status (called by Settlement Service)
     */
    public void updatePaymentStatusBatch(Long paymentId, PaymentStatus newStatus) {
        Payments payment = getPaymentById(paymentId);
        payment.setStatus(newStatus);
        paymentRepository.save(payment);
    }
}