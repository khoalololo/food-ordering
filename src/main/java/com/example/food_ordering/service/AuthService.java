package com.example.food_ordering.service;

import com.example.food_ordering.dto.request.RegisterRequest;
import com.example.food_ordering.entity.User;
import com.example.food_ordering.enums.Role;
import com.example.food_ordering.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public String register(RegisterRequest request){
        if (userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("Email is already in use!");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(Role.CUSTOMER)
                .build();

        userRepository.save(user);

        return "User registered successfully!";    }
}
