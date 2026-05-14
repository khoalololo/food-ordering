package com.example.food_ordering.service.payment;

import com.example.food_ordering.entity.Order;
import org.springframework.stereotype.Component;

@Component // Spring manages this as a bean — injectable anywhere
public class CashPaymentStrategy implements PaymentStrategy{
    @Override
    public String pay(Order order){
        // In real life: record cash intent, wait for confirmation
        return "CASH-" + System.currentTimeMillis() + "-" + order.getId();
    }
    @Override
    public String getMethodName() {
        return "CASH";
    }
}
