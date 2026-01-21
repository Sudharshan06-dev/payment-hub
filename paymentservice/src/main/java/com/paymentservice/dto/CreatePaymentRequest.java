package com.paymentservice.dto;
import java.math.BigDecimal;

public class CreatePaymentRequest {
    
    private Long userId;
    private Long billId;
    private Long accountId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    
    public CreatePaymentRequest(Long userId, Long billId, Long accountId, 
                                BigDecimal amount, String currency, String paymentMethod) {
        this.userId = userId;
        this.billId = billId;
        this.accountId = accountId;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
    }
    
    // Getters & Setters
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getBillId() {
        return billId;
    }
    
    public void setBillId(Long billId) {
        this.billId = billId;
    }
    
    public Long getAccountId() {
        return accountId;
    }
    
    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}