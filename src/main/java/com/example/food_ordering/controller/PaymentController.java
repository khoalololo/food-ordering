package com.example.food_ordering.controller;

import com.example.food_ordering.dto.request.ProcessPaymentRequest;
import com.example.food_ordering.dto.response.PaymentResponse;
import com.example.food_ordering.entity.User;
import com.example.food_ordering.repository.UserRepository;
import com.example.food_ordering.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<PaymentResponse> process(
            @Valid @RequestBody ProcessPaymentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(paymentService.process(request, user));
    }
}