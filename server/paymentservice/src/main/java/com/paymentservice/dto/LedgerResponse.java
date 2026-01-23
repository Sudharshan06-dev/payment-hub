package com.paymentservice.dto;

public class LedgerResponse {
    private boolean success;
    private String message;
    private Object data;
    
    //Returns collection instances so we need object for the data
    private LedgerResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
    
    public static LedgerResponse success(String message, Object data) {
        return new LedgerResponse(true, message, data);
    }
    
    public static LedgerResponse error(String message) {
        return new LedgerResponse(false, message, null);
    }
    
    // Getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Object getData() { return data; }
}
