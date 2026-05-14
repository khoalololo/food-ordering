package com.example.food_ordering.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="order_items")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The "owned" side of the relationship — this is where the foreign key column lives
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="food_id", nullable = false)
    private Food food;

    @Column(nullable = false)
    private int quantity;

    // Snapshot of the price at the time of ordering
    // Critical: food price may change later — we store what the customer actually paid
    @Column(nullable = false)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private BigDecimal subtotal; // unitPrice * quantity

    // Store toppings as a simple comma-separated string
    // For a real app you'd make a Topping entity, but this keeps it simple
    @Column(columnDefinition = "TEXT")
    private String toppings; // e.g. "Extra Cheese,Spicy"
}
