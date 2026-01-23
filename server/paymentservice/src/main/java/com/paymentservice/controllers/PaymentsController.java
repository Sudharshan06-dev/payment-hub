package com.paymentservice.controllers;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.paymentservice.dto.CreatePaymentRequest;
import com.paymentservice.dto.PaymentResponse;
import com.paymentservice.dto.UpdatePaymentStatusRequest;
import com.paymentservice.models.Payments;
import com.paymentservice.models.Payments.PaymentStatus;
import com.paymentservice.services.PaymentsService;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentsController {

    @Autowired
    private PaymentsService paymentsService;

    /**
     * Get all payments for a specific user (paginated)
     * GET /api/v1/payments?userId=xxx&page=0&size=10
     */
    @GetMapping
    public ResponseEntity<PaymentResponse> getAllPayments(
            @RequestParam(value = "userId") Long userId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Payments> payments = paymentsService.getAllPaymentsByUser(userId, pageable);
            
            return ResponseEntity.ok(
                PaymentResponse.success("Payments retrieved successfully", payments.getContent())
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                PaymentResponse.error("Failed to retrieve payments: " + e.getMessage())
            );
        }
    }

    /**
     * Get a specific payment by ID
     * GET /api/v1/payments/{paymentId}
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long paymentId) {
        try {
            Payments payment = paymentsService.getPaymentById(paymentId);
            return ResponseEntity.ok(
                PaymentResponse.success("Payment retrieved successfully", payment)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                PaymentResponse.error("Payment not found: " + e.getMessage())
            );
        }
    }

    /**
     * Create a new payment
     * POST /api/v1/payments
     * Body: {
     *   "userId": "xxx",
     *   "billId": "xxx",
     *   "accountId": "xxx",
     *   "amount": 100.00,
     *   "currency": "USD",
     *   "paymentMethod": "BANK_TRANSFER"
     * }
     */
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody CreatePaymentRequest request) {
        try {
            Payments payment = paymentsService.createPayment(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                PaymentResponse.success("Payment created successfully", payment)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                PaymentResponse.error("Failed to create payment: " + e.getMessage())
            );
        }
    }

    /**
     * Get payments by status (e.g., INITIATED, SETTLED, FAILED)
     * GET /api/v1/payments/status/INITIATED
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<PaymentResponse> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        try {
            List<Payments> payments = paymentsService.getPaymentsByStatus(status);
            return ResponseEntity.ok(
                PaymentResponse.success("Payments retrieved successfully", payments)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                PaymentResponse.error("Failed to retrieve payments: " + e.getMessage())
            );
        }
    }

    /**
     * Get user's payments with specific status
     * GET /api/v1/payments/user/{userId}/status/{status}
     */
    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<PaymentResponse> getUserPaymentsByStatus(
            @PathVariable Long userId,
            @PathVariable PaymentStatus status) {
        try {
            List<Payments> payments = paymentsService.getUserPaymentsByStatus(userId, status);
            return ResponseEntity.ok(
                PaymentResponse.success("Payments retrieved successfully", payments)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                PaymentResponse.error("Failed to retrieve payments: " + e.getMessage())
            );
        }
    }

    /**
     * Update payment status
     * PUT /api/v1/payments/{paymentId}/status
     * Body: {
     *   "status": "SETTLED"
     * }
     */
    @PutMapping("/{paymentId}/status")
    public ResponseEntity<PaymentResponse> updatePaymentStatus(
            @PathVariable Long paymentId,
            @RequestBody UpdatePaymentStatusRequest request) {
        try {
            Payments payment = paymentsService.updatePaymentStatus(paymentId, request.getStatus());
            return ResponseEntity.ok(
                PaymentResponse.success("Payment status updated successfully", payment)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                PaymentResponse.error("Failed to update payment: " + e.getMessage())
            );
        }
    }

    /**
     * Delete a payment (only if INITIATED)
     * DELETE /api/v1/payments/{paymentId}
     */
    @DeleteMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> deletePayment(@PathVariable Long paymentId) {
        try {
            paymentsService.deletePayment(paymentId);
            return ResponseEntity.ok(
                PaymentResponse.success("Payment deleted successfully", null)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                PaymentResponse.error("Failed to delete payment: " + e.getMessage())
            );
        }
    }

    /**
     * Get payments for batch processing (all INITIATED payments)
     * GET /api/v1/payments/batch/pending
     */
    @GetMapping("/batch/pending")
    public ResponseEntity<PaymentResponse> getPendingPaymentsForBatch() {
        try {
            List<Payments> payments = paymentsService.getPendingPaymentsForBatch();
            return ResponseEntity.ok(
                PaymentResponse.success("Pending payments retrieved for batch", payments)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                PaymentResponse.error("Failed to retrieve pending payments: " + e.getMessage())
            );
        }
    }
}