package com.example.food_ordering.service;

import com.example.food_ordering.entity.Payment;
import org.springframework.context.ApplicationEvent;

public class PaymentSuccessEvent extends ApplicationEvent {
    private final Payment payment;
    public PaymentSuccessEvent(Object source, Payment payment) {
        super(source);
        this.payment = payment;
    }
    public Payment getPayment() { return payment; }
}