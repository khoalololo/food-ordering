package com.example.food_ordering.service;

import com.example.food_ordering.entity.Food;
import com.example.food_ordering.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FoodService {
    private final FoodRepository foodRepository;
    public List<Food> getAll(){
        return foodRepository.findByIsAvailableTrueOrderByTypeAscNameAsc();
    }

    public List<Food> getAllIncludingUnavailable() {
        return foodRepository.findAllByOrderByTypeAscNameAsc();
    }
    public Food getById(Long id){
        // orElseThrow is the idiomatic Java way to handle not-found
        // we'll wire this to a 404 response via an exception handler later
        return foodRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Food not found: " + id));
    }

    public Food create(Food food) {
        return foodRepository.save(food);
    }

    public Food update(Long id, Food updated){
        Food existing = getById(id);
        existing.setName(updated.getName());
        existing.setBasePrice(updated.getBasePrice());
        existing.setType(updated.getType());
        existing.setIsAvailable(updated.getIsAvailable());
        existing.setImageUrl(updated.getImageUrl());
        existing.setDescription(updated.getDescription());
        return foodRepository.save(existing);
    }

    public void delete (Long id){
        foodRepository.deleteById(id);
    }
}
