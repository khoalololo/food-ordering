package com.example.food_ordering.repository;

import com.example.food_ordering.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FoodRepository extends JpaRepository<Food, Long> {
    // Spring reads this name and generates:
    // SELECT * FROM foods WHERE is_available = true ORDER BY type, name
    List<Food> findByIsAvailableTrueOrderByTypeAscNameAsc();

    // SELECT * FROM foods WHERE type =?
    List<Food> findByType(String type);

    // SELECT * FROM foods WHERE LOWER(name) LIKE LOWER('%?%')
    List<Food> findByNameContainingIgnoreCase(String name);
}
