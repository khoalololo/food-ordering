package com.example.food_ordering.service.payment;

import com.example.food_ordering.entity.Order;
import org.springframework.stereotype.Component;

@Component
public class MomoPaymentStrategy implements PaymentStrategy {

    @Override
    public String pay(Order order) {
        // In real life: call MoMo payment gateway API here
        return "MOMO-" + System.currentTimeMillis() + "-" + order.getId();
    }

    @Override
    public String getMethodName() {
        return "MOMO";
    }
}