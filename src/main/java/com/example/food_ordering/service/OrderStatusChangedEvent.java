package com.example.food_ordering.service;

import com.example.food_ordering.entity.Order;
import org.springframework.context.ApplicationEvent;

public class OrderStatusChangedEvent extends ApplicationEvent {
    private final Order order;
    public OrderStatusChangedEvent(Object source, Order order) {
        super(source);
        this.order = order;
    }
    public Order getOrder() { return order; }
}
