package com.paymentservice.dto;

public class LedgerResponse {
    private String title;
    private String message;
    private Object data;
    
    //Returns collection instances so we need object for the data
    private LedgerResponse(String title, String message, Object data) {
        this.title = title;
        this.message = message;
        this.data = data;
    }
    
    public static LedgerResponse success(String message, Object data) {
        return new LedgerResponse("Sucess", message, data);
    }
    
    public static LedgerResponse error(String message) {
        return new LedgerResponse("Error", message, null);
    }
    
    // Getters
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public Object getData() { return data; }
}
