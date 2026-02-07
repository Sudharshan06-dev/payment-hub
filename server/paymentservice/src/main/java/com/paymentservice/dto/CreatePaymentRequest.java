package com.paymentservice.dto;
import java.math.BigDecimal;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreatePaymentRequest {
    
    private Long userId;
    private Long billId;
    private Long accountId;
    private String userEmail;
    private BigDecimal amount;
    private JsonNode paymentDetails;
    private String currency;
    private String paymentMethod;
}