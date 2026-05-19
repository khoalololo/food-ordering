package com.example.food_ordering.dto.request;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String fullName;
    private String email;
    private String phone;
}