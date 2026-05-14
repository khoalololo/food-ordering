package com.example.food_ordering.repository;

import com.example.food_ordering.entity.Order;
import com.example.food_ordering.entity.Payment;
import com.example.food_ordering.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long>{
    Optional<Payment> findByOrder(Order order);
    List<Payment> findByUserOrderByCreatedAtDesc( User user);
}
