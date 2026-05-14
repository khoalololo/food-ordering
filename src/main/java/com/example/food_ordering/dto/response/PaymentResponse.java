package com.example.food_ordering.dto.response;

import com.example.food_ordering.enums.PaymentMethod;
import com.example.food_ordering.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private PaymentMethod method;
    private BigDecimal amount;
    private PaymentStatus status;
    private String transactionId;
    private LocalDateTime paidAt;
}
