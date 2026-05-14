package com.example.food_ordering.service;

import com.example.food_ordering.dto.request.ProcessPaymentRequest;
import com.example.food_ordering.dto.response.PaymentResponse;
import com.example.food_ordering.entity.Order;
import com.example.food_ordering.entity.Payment;
import com.example.food_ordering.entity.User;
import com.example.food_ordering.enums.PaymentMethod;
import com.example.food_ordering.enums.PaymentStatus;
import com.example.food_ordering.repository.PaymentRepository;
import com.example.food_ordering.service.payment.PaymentStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final ApplicationEventPublisher eventPublisher;

    // Spring injects ALL PaymentStrategy beans as a List automatically
    // We build a Map so we can look up by method name — same idea as your PaymentAdapter
    private final List<PaymentStrategy> strategies;

    private PaymentStrategy getStrategy(PaymentMethod method) {
        return strategies.stream()
                .filter(s -> s.getMethodName().equals(method.name()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Unknown payment method: " + method));
    }

    @Transactional
    public PaymentResponse process(ProcessPaymentRequest request, User user) {
        Order order = orderService.getById(request.getOrderId());

        // Guard: already paid?
        paymentRepository.findByOrder(order).ifPresent(p -> {
            if (p.getStatus() == PaymentStatus.SUCCESS) {
                throw new RuntimeException("Order already paid");
            }
        });

        PaymentStrategy strategy = getStrategy(request.getMethod());
        String transactionId = strategy.pay(order); // Strategy pattern in action

        Payment payment = Payment.builder()
                .order(order)
                .user(user)
                .method(request.getMethod())
                .amount(order.getTotalAmount())
                .status(PaymentStatus.SUCCESS)
                .transactionId(transactionId)
                .paidAt(LocalDateTime.now())
                .build();

        Payment saved = paymentRepository.save(payment);

        // Notify via event
        eventPublisher.publishEvent(new PaymentSuccessEvent(this, saved));

        return toResponse(saved);
    }

    public PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .method(payment.getMethod())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .paidAt(payment.getPaidAt())
                .build();
    }
}