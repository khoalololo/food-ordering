package com.example.food_ordering.service;

import com.example.food_ordering.dto.request.UpdateRoleRequest;
import com.example.food_ordering.dto.request.UpdateUserRequest;
import com.example.food_ordering.dto.response.UserResponse;
import com.example.food_ordering.entity.User;
import com.example.food_ordering.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // Map entity to dto
    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }

    // Get all users
    public List<UserResponse> getAll() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Get single users
    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        return toResponse(user);
    }

    // Update profile field
    public UserResponse update(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));

        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getEmail()    != null) user.setEmail(request.getEmail());
        if (request.getPhone()    != null) user.setPhone(request.getPhone());

        return toResponse(userRepository.save(user));
    }


    // Update role, Used by manager to promote/demote staff
    public UserResponse updateRole(Long id, UpdateRoleRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        user.setRole(request.getRole());
        return toResponse(userRepository.save(user));
    }
}