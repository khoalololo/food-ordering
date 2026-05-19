package com.example.food_ordering.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private Long orderId;
    private String type;
    private String event;
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;
}
