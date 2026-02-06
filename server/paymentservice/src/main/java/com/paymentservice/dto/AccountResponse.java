package com.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard API Response wrapper for all REST endpoints
 * Ensures consistent response format across the application
 * 
 * Example response:
 * {
 *   "success": true,
 *   "message": "Account created successfully",
 *   "data": { ... }
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse<T> {
    
    /**
     * Indicates whether the request was successful
     */
    private boolean success;
    
    /**
     * Human-readable message describing the result
     */
    private String message;
    
    /**
     * The actual response data (can be any type)
     * Will be null if request was unsuccessful
     */
    private T data;
    
    /**
     * Convenience method for success responses
     */
    public static <T> AccountResponse<T> success(String message, T data) {
        return AccountResponse.<T>builder()
            .success(true)
            .message(message)
            .data(data)
            .build();
    }
    
    /**
     * Convenience method for error responses
     */
    public static <T> AccountResponse<T> error(String message) {
        return AccountResponse.<T>builder()
            .success(false)
            .message(message)
            .data(null)
            .build();
    }
}