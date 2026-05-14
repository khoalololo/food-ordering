package com.example.food_ordering.repository;

import com.example.food_ordering.entity.Notification;
import com.example.food_ordering.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long>{
    List<Notification> findByUserOrderByCreatedDesc(User user);
    List<Notification> findByTypeOrderByCreatedAtDesc(String type); // for KITCHEN

    long countByUserAndIsReadFalse(User user);
}
