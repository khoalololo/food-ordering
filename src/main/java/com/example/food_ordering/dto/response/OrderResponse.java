package com.example.food_ordering.dto.response;

import com.example.food_ordering.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

//This is what the client actually sees, not the raw entity
@Data
@Builder
public class OrderResponse {
    private Long id;
    private Long userId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;

    @Data
    @Builder
    public static class OrderItemResponse{
        private Long foodId;
        private String foodName;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
        private String toppings;
    }
}
