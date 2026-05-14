package com.example.food_ordering.dto.request;

import com.example.food_ordering.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProcessPaymentRequest {
    @NotNull
    private Long orderId;
    @NotNull
    private PaymentMethod method;
    private String couponCode;
}
