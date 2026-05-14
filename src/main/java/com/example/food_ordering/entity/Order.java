package com.example.food_ordering.entity;

import com.example.food_ordering.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name ="orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ManyToOne: many orders can belong to one user
    // FetchType.LAZY = don't load User from DB unless we explicitly call order.getUser()
    // This is almost always what I want: avoids unnecessary queries
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    // OneToMany: one order has many items
    // cascade = ALL means: if we save/delete an Order, JPA automatically saves/deletes its items too
    // orphanRemoval = true means: if we remove an item from the list, JPA deletes it from DB
    // mappedBy = "order" tells JPA: the foreign key lives on the OrderItem side (in the "order" field)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // Helper method — keeps the bidirectional relationship in sync
    // Always add helpers like this when you have bidirectional relationships
    public void addItem(OrderItem item){
        items.add(item);
        item.setOrder(this); //keep both sides consistent
    }

    public void removeItem(OrderItem item){
        items.remove(item);
        item.setOrder(null);
    }
}
