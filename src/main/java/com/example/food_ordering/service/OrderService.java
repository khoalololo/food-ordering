package com.example.food_ordering.service;

import com.example.food_ordering.dto.request.CreateOrderRequest;
import com.example.food_ordering.dto.response.OrderResponse;
import com.example.food_ordering.entity.Food;
import com.example.food_ordering.entity.Order;
import com.example.food_ordering.entity.OrderItem;
import com.example.food_ordering.entity.User;
import com.example.food_ordering.enums.OrderStatus;
import com.example.food_ordering.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final FoodService foodService;
    private final ApplicationEventPublisher eventPublisher; // Spring's built-in Observer

    // @Transactional: if anything inside throws an exception, the whole DB operation rolls back
    // This is critical, you never want a half-saved order in your database
    @Transactional
    public Order createOrder(CreateOrderRequest request, User user){
        Order order = Order.builder()
                .user(user)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (CreateOrderRequest.OrderItemRequest itemReq: request.getItems()){
            Food food = foodService.getById(itemReq.getFoodId());
            BigDecimal unitPrice = food.getBasePrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            OrderItem item = OrderItem.builder()
                    .food(food)
                    .foodName(food.getName())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .toppings(itemReq.getToppings() != null
                            ? String.join(",", itemReq.getToppings())
                            : null)
                    .build();

            order.addItem(item); // uses our helper method to keeps both sides in sync
            total = total.add(subtotal);
        }
        order.setTotalAmount(total);
        Order saved = orderRepository.save(order);
        // Publish event — NotificationService listens
        // This is the Spring equivalent of your OrderSubject.notify()
        eventPublisher.publishEvent(new OrderCreatedEvent(this, saved));
        return saved;
    }

    @Transactional
    public  Order advanceStatus(Long orderId){
        Order order = getById(orderId);

        // State machine logic — The same idea as your Node.js OrderState pattern
        OrderStatus next = switch (order.getStatus()){
            case PENDING -> OrderStatus.CONFIRMED;
            case CONFIRMED -> OrderStatus.PREPARING;
            case PREPARING -> OrderStatus.READY;
            case READY -> OrderStatus.COMPLETED;
            default -> throw new RuntimeException(
                    "Cannot advance order in status: " + order.getStatus()
            );
        };

        order.setStatus(next);
        Order saved = orderRepository.save(order);
        eventPublisher.publishEvent(new OrderStatusChangedEvent(this, saved));
        return saved;
    };

    @Transactional
    public Order cancelOrder(Long orderId){
        Order order = getById(orderId);
        if (!List.of(OrderStatus.PENDING, OrderStatus.CONFIRMED).contains(order.getStatus())) {
            throw new RuntimeException("Cannot cancel order in status: " + order.getStatus());
        }
        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);
        eventPublisher.publishEvent(new OrderCancelledEvent(this, saved));
        return saved;
    }

    public Order getById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
    }
    public List<Order> getOrdersForUser(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }
    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    // Map entity → DTO to keeps entity out of controller layer
    public OrderResponse toResponse(Order order) {
        List<OrderResponse.OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderResponse.OrderItemResponse.builder()
                        .foodId(item.getFood().getId())
                        .foodName(item.getFoodName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .toppings(item.getToppings())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .build();
    }
}
