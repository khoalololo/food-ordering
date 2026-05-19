package com.example.food_ordering.service;

import com.example.food_ordering.entity.Notification;
import com.example.food_ordering.entity.Order;
import com.example.food_ordering.entity.Payment;
import com.example.food_ordering.entity.User;
import com.example.food_ordering.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    // @EventListener is Spring's Observer pattern
    // When OrderService publishes OrderCreatedEvent, Spring calls this automatically
    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        Order order = event.getOrder();
        User user = order.getUser();

        // Notify the customer
        save(user, order, "USER", "ORDER_CREATED",
                "Your order #" + order.getId() + " has been placed. Please wait for confirmation.");

        // Notify the kitchen
        save(null, order, "KITCHEN", "ORDER_CREATED",
                "New order #" + order.getId() + " needs confirmation!");
    }

    @EventListener
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        Order order = event.getOrder();
        save(order.getUser(), order, "USER", "ORDER_STATUS_CHANGED",
                "Your order #" + order.getId() + " is now: " + order.getStatus());
    }

    @EventListener
    public void onOrderCancelled(OrderCancelledEvent event) {
        Order order = event.getOrder();
        save(order.getUser(), order, "USER", "ORDER_CANCELLED",
                "Your order #" + order.getId() + " has been cancelled.");
        save(null, order, "KITCHEN", "ORDER_CANCELLED",
                "Order #" + order.getId() + " was cancelled — stop preparation.");
    }

    @EventListener
    public void onPaymentSuccess(PaymentSuccessEvent event) {
        Payment payment = event.getPayment();
        Order order = payment.getOrder();
        save(order.getUser(), order, "USER", "ORDER_PAID",
                "Payment for order #" + order.getId() +
                        " succeeded. Transaction: " + payment.getTransactionId());
        save(null, order, "KITCHEN", "ORDER_PAID",
                "Order #" + order.getId() + " is paid — start preparing!");
    }

    private void save(User user, Order order, String type, String event, String message) {
        notificationRepository.save(
                Notification.builder()
                        .user(user)
                        .order(order)
                        .type(type)
                        .event(event)
                        .message(message)
                        .build()
        );
    }

    public List<Notification> getForUser(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<Notification> getKitchenNotifications() {
        return notificationRepository.findByTypeOrderByCreatedAtDesc("KITCHEN");
    }

    public void markAsRead(Long id) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        n.setRead(true);
        notificationRepository.save(n);
    }

    public void markAllAsRead(User user) {
        List<Notification> unread = notificationRepository.findByUserAndIsReadFalse(user);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    public long countUnread(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }
}
