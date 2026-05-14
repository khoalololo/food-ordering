package com.example.food_ordering.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {

    @NotEmpty(message = "Order must have at least one item")
    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest{
        @NotNull
        private long foodId;

        @Positive(message = "Quantity must be at least 1")
        private int quantity;
        private List<String> toppings;
    }
}
