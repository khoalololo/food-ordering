package com.example.food_ordering.controller;

import com.example.food_ordering.dto.request.RegisterRequest;
import com.example.food_ordering.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request){
        String result = authService.register(request);
        return ResponseEntity.ok(result);
    }
}
