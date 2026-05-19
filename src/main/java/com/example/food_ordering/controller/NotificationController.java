package com.example.food_ordering.controller;

import com.example.food_ordering.dto.response.NotificationResponse;
import com.example.food_ordering.entity.Notification;
import com.example.food_ordering.entity.User;
import com.example.food_ordering.repository.UserRepository;
import com.example.food_ordering.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    private User currentUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/my")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        User user = currentUser(userDetails);
        List<NotificationResponse> list = notificationService.getForUser(user)
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/kitchen")
    public ResponseEntity<List<NotificationResponse>> getKitchenNotifications() {
        List<NotificationResponse> list = notificationService.getKitchenNotifications()
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(list);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllRead(@AuthenticationPrincipal UserDetails userDetails) {
        User user = currentUser(userDetails);
        notificationService.markAllAsRead(user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal UserDetails userDetails) {
        User user = currentUser(userDetails);
        return ResponseEntity.ok(notificationService.countUnread(user));
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .orderId(n.getOrder() != null ? n.getOrder().getId() : null)
                .type(n.getType())
                .event(n.getEvent())
                .message(n.getMessage())
                .isRead(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
