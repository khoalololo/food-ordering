package com.example.food_ordering.service;

import com.example.food_ordering.dto.request.LoginRequest;
import com.example.food_ordering.dto.request.RegisterRequest;
import com.example.food_ordering.dto.response.AuthResponse;
import com.example.food_ordering.entity.User;
import com.example.food_ordering.enums.Role;
import com.example.food_ordering.repository.UserRepository;
import com.example.food_ordering.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    //register
    public AuthResponse register(RegisterRequest request){
        if (userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("Email is already in use!");
        }

        Role role = Role.CUSTOMER;
        if (request.getRole() != null) {
            try {
                role = Role.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(role)
                .build();

        User saved = userRepository.save(user);
        String token = jwtUtil.generate(saved.getEmail());

        return buildResponse(token, saved);
    }

    public AuthResponse login(LoginRequest request){
        try {
            // AuthenticationManager validates email + password against the DB via UserDetailsServiceImpl
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Invalid email or password");
        }
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String token = jwtUtil.generate(user.getEmail());
        return buildResponse(token, user);
    }

    private AuthResponse buildResponse(String token, User user) {
        return AuthResponse.builder()
                .token(token)
                .user(AuthResponse.UserPayload.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .phone(user.getPhone())
                        .role(user.getRole())
                        .createdAt(user.getCreatedAt())
                        .build())
                .build();
    }
}
