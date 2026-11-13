package com.developersfab.Dhanushappbe.food;

import com.developersfab.Dhanushappbe.common.CategoryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/food-categories")
@RequiredArgsConstructor
public class FoodCategoryController {

    private final FoodService foodService;

    @GetMapping
    public ResponseEntity<List<CategoryDto>> listFoodCategories() {
        List<CategoryDto> categories = foodService.listCategories();
        return ResponseEntity.ok(categories);
    }
}

