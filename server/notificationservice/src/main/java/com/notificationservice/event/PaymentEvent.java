package com.notificationservice.event;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {

    private String transactionReference;
    private String paymentMethod;
    private String userEmail;
    private BigDecimal paymentAmount;
    private LocalDateTime paymentDate;
}