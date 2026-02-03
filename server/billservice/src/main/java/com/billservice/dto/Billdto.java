package com.billservice.dto;

import java.time.LocalDateTime;

import com.billservice.models.Bills;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder
public class Billdto {

    @JsonProperty("bill_id")
    private Long billId;
    
    @JsonProperty("user_id")
    private Long userId;
    
    @JsonProperty("biller_name")
    private String billerName;
    
    @JsonProperty("account_number")
    private String accountNumber;
    
    @JsonProperty("amount")
    private Double amount;
    
    @JsonProperty("currency")
    private String currency;
    
    @JsonProperty("due_date")
    private LocalDateTime dueDate;
    
    @JsonProperty("bill_status")
    private Bills.BillStatus billStatus;
    
    @JsonProperty("bill_frequency")
    private Bills.BillFrequency billFrequency;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    
}
