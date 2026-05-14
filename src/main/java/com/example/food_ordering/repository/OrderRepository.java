package com.example.food_ordering.repository;

import com.example.food_ordering.entity.Order;
import com.example.food_ordering.entity.User;
import com.example.food_ordering.enums.OrderStatus;
import org.springframework.boot.webmvc.autoconfigure.WebMvcProperties;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // All orders for a specific user, newest first
    List<Order> findByUserOrderByCreatedAtDesc(User user);


    // Staff view — all orders with a specific status
    List<Order> findByStatusOrderByCreatedAtAsc(OrderStatus status);

    // All orders, newest first (staff/manager view)
    List<Order> findAllByOrderByCreatedAtDesc();
}
