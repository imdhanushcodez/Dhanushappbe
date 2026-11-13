package com.developersfab.Dhanushappbe.food;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/foods")
@RequiredArgsConstructor
public class FoodController {

    private final FoodService foodService;

    @GetMapping
    public ResponseEntity<List<FoodResponse>> listFoods(@RequestParam(required = false) Integer categoryId) {
        List<FoodResponse> foods = foodService.listFoods(categoryId);
        return ResponseEntity.ok(foods);
    }

    @PostMapping
    public ResponseEntity<FoodResponse> createFood(@Valid @RequestBody FoodRequest request) {
        FoodResponse created = foodService.createFood(request);
        URI location = URI.create("/api/foods/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FoodResponse> getFood(@PathVariable Long id) {
        FoodResponse food = foodService.getFood(id);
        return ResponseEntity.ok(food);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FoodResponse> updateFood(
            @PathVariable Long id,
            @Valid @RequestBody FoodRequest request) {
        FoodResponse updated = foodService.updateFood(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFood(@PathVariable Long id) {
        foodService.deleteFood(id);
        return ResponseEntity.noContent().build();
    }
}

