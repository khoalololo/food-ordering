package com.example.food_ordering.controller;

import com.example.food_ordering.dto.request.CreateOrderRequest;
import com.example.food_ordering.dto.response.OrderResponse;
import com.example.food_ordering.entity.Order;
import com.example.food_ordering.entity.User;
import com.example.food_ordering.repository.UserRepository;
import com.example.food_ordering.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    // @Valid triggers the validation annotations on CreateOrderRequest
    @PostMapping
    public ResponseEntity<OrderResponse> create(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow();
        Order order = orderService.createOrder(request, user);
        return ResponseEntity.ok(orderService.toResponse(order));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.toResponse(orderService.getById(id)));
    }

    @GetMapping("/my")
    public ResponseEntity<List<OrderResponse>> myOrders(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        List<OrderResponse> orders = orderService.getOrdersForUser(user)
                .stream().map(orderService::toResponse).toList();
        return ResponseEntity.ok(orders);
    }

    @PatchMapping("/{id}/advance")
    public ResponseEntity<OrderResponse> advance(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.toResponse(orderService.advanceStatus(id)));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.toResponse(orderService.cancelOrder(id)));
    }
}