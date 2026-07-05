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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final FoodService foodService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Order createOrder(CreateOrderRequest request, User user) {
        Order order = Order.builder()
                .user(user)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (CreateOrderRequest.OrderItemRequest itemReq : request.getItems()) {
            Food food = foodService.getById(itemReq.getFoodId());

            List<String> toppings = normalizeToppings(itemReq.getToppings());

            BigDecimal basePrice = food.getBasePrice();
            BigDecimal toppingTotal = calculateToppingTotal(food.getType(), toppings);
            BigDecimal unitPrice = basePrice.add(toppingTotal);

            BigDecimal subtotal = unitPrice.multiply(
                    BigDecimal.valueOf(itemReq.getQuantity())
            );

            OrderItem item = OrderItem.builder()
                    .food(food)
                    .foodName(food.getName())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .toppings(!toppings.isEmpty() ? String.join(",", toppings) : null)
                    .build();

            order.addItem(item);
            total = total.add(subtotal);
        }

        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);

        eventPublisher.publishEvent(new OrderCreatedEvent(this, saved));

        return saved;
    }

    private List<String> normalizeToppings(List<String> toppings) {
        if (toppings == null || toppings.isEmpty()) {
            return List.of();
        }

        List<String> result = new ArrayList<>();

        for (String topping : toppings) {
            if (topping == null || topping.isBlank()) {
                continue;
            }

            String[] parts = topping.split(",");

            for (String part : parts) {
                String clean = part.trim();

                if (!clean.isBlank()) {
                    result.add(clean);
                }
            }
        }

        return result;
    }

   private BigDecimal calculateToppingTotal(String foodType, List<String> toppings) {
    return toppings.stream()
            .map(topping -> getToppingPrice(foodType, topping))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
}

private BigDecimal getToppingPrice(String foodType, String topping) {
    if (foodType == null || topping == null) {
        return BigDecimal.ZERO;
    }

    String type = foodType.trim().toLowerCase();
    String name = topping.trim().toLowerCase();

    return switch (type) {
        case "burger" -> switch (name) {
            case "extra cheese" -> BigDecimal.valueOf(10000);
            case "extra meat" -> BigDecimal.valueOf(20000);
            case "extra veggies" -> BigDecimal.valueOf(8000);
            case "no onion" -> BigDecimal.ZERO;
            default -> BigDecimal.ZERO;
        };

        case "pizza" -> switch (name) {
            case "extra cheese" -> BigDecimal.valueOf(15000);
            case "more pepperoni" -> BigDecimal.valueOf(20000);
            case "mushroom" -> BigDecimal.valueOf(10000);
            case "thin crust" -> BigDecimal.ZERO;
            default -> BigDecimal.ZERO;
        };

        case "pasta" -> switch (name) {
            case "parmesan cheese" -> BigDecimal.valueOf(12000);
            case "extra bacon" -> BigDecimal.valueOf(18000);
            case "extra sauce" -> BigDecimal.valueOf(8000);
            default -> BigDecimal.ZERO;
        };

        case "salad" -> switch (name) {
            case "grilled chicken" -> BigDecimal.valueOf(18000);
            case "extra dressing" -> BigDecimal.valueOf(5000);
            case "croutons" -> BigDecimal.valueOf(7000);
            default -> BigDecimal.ZERO;
        };

        case "soup" -> switch (name) {
            case "garlic bread" -> BigDecimal.valueOf(10000);
            case "extra cream" -> BigDecimal.valueOf(6000);
            default -> BigDecimal.ZERO;
        };

        case "dessert" -> switch (name) {
            case "ice cream" -> BigDecimal.valueOf(12000);
            case "extra chocolate" -> BigDecimal.valueOf(7000);
            default -> BigDecimal.ZERO;
        };

        case "drink" -> switch (name) {
            case "extra ice", "no sugar" -> BigDecimal.ZERO;
            default -> BigDecimal.ZERO;
        };

        default -> BigDecimal.ZERO;
    };
}

    @Transactional
    public Order advanceStatus(Long orderId) {
        Order order = getById(orderId);

        OrderStatus next = switch (order.getStatus()) {
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
    }

    @Transactional
    public Order cancelOrder(Long orderId) {
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