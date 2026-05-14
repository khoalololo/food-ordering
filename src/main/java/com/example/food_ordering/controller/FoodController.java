package com.example.food_ordering.controller;

import com.example.food_ordering.entity.Food;
import com.example.food_ordering.service.FoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/food")
@RequiredArgsConstructor
public class FoodController {

    private final FoodService foodService;

    @GetMapping
    public ResponseEntity<List<Food>> getAll() {
        return ResponseEntity.ok(foodService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Food> getById(@PathVariable Long id) {
        return ResponseEntity.ok(foodService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Food> create(@RequestBody Food food) {
        return ResponseEntity.ok(foodService.create(food));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Food> update(@PathVariable Long id, @RequestBody Food food) {
        return ResponseEntity.ok(foodService.update(id, food));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        foodService.delete(id);
        return ResponseEntity.noContent().build();
    }
}