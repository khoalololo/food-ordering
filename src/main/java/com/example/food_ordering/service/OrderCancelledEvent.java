package com.example.food_ordering.service;

import com.example.food_ordering.entity.Order;
import org.springframework.context.ApplicationEvent;

public class OrderCancelledEvent extends ApplicationEvent {
    private final Order order;
    public OrderCancelledEvent(Object source, Order order) {
        super(source);
        this.order = order;
    }
    public Order getOrder() { return order; }
}