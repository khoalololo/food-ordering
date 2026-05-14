package com.example.food_ordering.service.payment;

import com.example.food_ordering.entity.Order;
import org.springframework.stereotype.Component;

@Component
public class BankPaymentStrategy implements PaymentStrategy {

    @Override
    public String pay(Order order) {
        // In real life: call bank transfer API here
        return "BANK-" + System.currentTimeMillis() + "-" + order.getId();
    }

    @Override
    public String getMethodName() {
        return "BANK";
    }
}