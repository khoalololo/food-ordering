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
import org.springframework.security.access.prepost.PreAuthorize;
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

    // Create current user from Spring Security context
    private User currentUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Get customers' owned order
    @GetMapping("/my")
    public ResponseEntity<List<OrderResponse>> myOrders (@AuthenticationPrincipal UserDetails userDetails) {
        User user = currentUser(userDetails);
        List<OrderResponse> orders = orderService.getOrdersForUser(user)
                .stream()
                .map(orderService::toResponse)
                .toList();
        return ResponseEntity.ok(orders);
    }

    // Get all orders for staff/manager
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('STAFF','MANAGER')")
    public ResponseEntity<List<OrderResponse>> allOrders(){
        List<OrderResponse> orders = orderService.getAllOrders()
                .stream()
                .map(orderService::toResponse)
                .toList();
        return ResponseEntity.ok(orders);
    }

    // Get single order (customer sees own, staff sees any)
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {

        User user = currentUser(userDetails);
        var order = orderService.getById(id);

        //Customer can only see their own orders
        boolean isStaffOrAbove = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STAFF")
                        || a.getAuthority().equals("ROLE_MANAGER"));

        if (!isStaffOrAbove && order.getUser().getId() != user.getId()) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(orderService.toResponse(order));
    }

    // Create order
    @PostMapping
    public ResponseEntity<OrderResponse> create(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = currentUser(userDetails);
        var order = orderService.createOrder(request, user);
        return ResponseEntity.status(201).body(orderService.toResponse(order));
    }

    // Patch, move to next status
    @PatchMapping("/{id}/advance")
    @PreAuthorize("hasAnyRole('STAFF','MANAGER')")
    public ResponseEntity<OrderResponse> advance(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.toResponse(orderService.advanceStatus(id)));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancel(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {

        User user = currentUser(userDetails);
        var order = orderService.getById(id);
        boolean isStaffOrAbove = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STAFF")
                        || a.getAuthority().equals("ROLE_MANAGER"));

        // Only the owner or staff can cancel
        if (!isStaffOrAbove && order.getUser().getId() != user.getId()) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(orderService.toResponse(orderService.cancelOrder(id)));
    }
}