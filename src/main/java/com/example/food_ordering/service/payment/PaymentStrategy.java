package com.example.food_ordering.service.payment;

import com.example.food_ordering.entity.Order;

public interface PaymentStrategy {
    String pay(Order order); // returns a transactionId
    String getMethodName();
}
