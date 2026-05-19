package com.example.food_ordering.dto.response;

import com.example.food_ordering.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

// This is what the frontend receives after sign-in or register
// The frontend stores token in localStorage and user for display
@Data
@Builder
public class AuthResponse {

    private String token;
    private UserPayload user;  // Nested user object as frontend reads user.role to decide which nav to show}

    @Data
    @Builder
    public static class UserPayload{
        private Long id;
        private String email;
        private String fullName;
        private String phone;
        private Role role;
        private LocalDateTime createdAt;
    }
}
