package com.paymentservice.dto;

public class PaymentResponse {
    private boolean success;
    private String message;
    private Object data;
    
    private PaymentResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
    
    public static PaymentResponse success(String message, Object data) {
        return new PaymentResponse(true, message, data);
    }
    
    public static PaymentResponse error(String message) {
        return new PaymentResponse(false, message, null);
    }
    
    // Getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Object getData() { return data; }
}
