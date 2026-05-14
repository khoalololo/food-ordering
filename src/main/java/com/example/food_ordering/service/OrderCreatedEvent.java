package com.example.food_ordering.service;

import com.example.food_ordering.entity.Order;
import org.springframework.context.ApplicationEvent;

// Simple event objects
public class OrderCreatedEvent extends ApplicationEvent {
    private final Order order;
    public OrderCreatedEvent(Object source, Order order) {
        super(source);
        this.order = order;
    }
    public Order getOrder() { return order; }
}
