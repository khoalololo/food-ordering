package com.example.food_ordering.dto.request;

import com.example.food_ordering.enums.Role;
import lombok.Data;

@Data
public class UpdateRoleRequest {
    private Role role;
}