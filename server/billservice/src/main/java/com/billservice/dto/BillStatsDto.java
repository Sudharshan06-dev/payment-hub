package com.billservice.dto;
import java.util.ArrayList;
import com.billservice.models.Bills;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder
public class BillStatsDto {

    @JsonProperty("total")
    private Long total;
    
    @JsonProperty("pending")
    private Long pending;
    
    @JsonProperty("paid")
    private Long paid;
    
    @JsonProperty("overdue")
    private Long overdue;
    
    @JsonProperty("due")
    private Long due;
    
    @JsonProperty("total_amount")
    private Double totalAmount;
    
    @JsonProperty("bills")
    private ArrayList<Bills> bills;
    
}
