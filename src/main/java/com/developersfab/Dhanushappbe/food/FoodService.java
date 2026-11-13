package com.developersfab.Dhanushappbe.food;

import com.developersfab.Dhanushappbe.common.AppException;
import com.developersfab.Dhanushappbe.common.CategoryDto;
import com.developersfab.Dhanushappbe.common.ErrorCode;
import com.developersfab.Dhanushappbe.plan.FoodPlanEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FoodService {

    private final FoodCategoryRepository categoryRepository;
    private final FoodRepository foodRepository;
    private final FoodPlanEntryRepository planEntryRepository;

    public List<CategoryDto> listCategories() {
        return categoryRepository.findAllByOrderByIdAsc().stream()
                .map(c -> new CategoryDto(c.getId(), c.getName()))
                .toList();
    }

    public List<FoodResponse> listFoods(Integer categoryId) {
        return foodRepository.findAllByCategoryIdOrder(categoryId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public FoodResponse createFood(FoodRequest request) {
        FoodCategory category = getCategory(request.categoryId());

        Food food = Food.builder()
                .category(category)
                .dishName(request.dishName().trim())
                .weightG(request.weightG())
                .calories(request.calories())
                .proteinG(request.proteinG())
                .fiberG(request.fiberG())
                .imageUrl(request.imageUrl() != null && !request.imageUrl().isBlank() ? request.imageUrl().trim() : null)
                .others(request.others() != null && !request.others().isBlank() ? request.others().trim() : null)
                .build();

        Food saved = foodRepository.save(food);
        return toResponse(saved);
    }

    public FoodResponse getFood(Long id) {
        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "Food not found."));
        return toResponse(food);
    }

    @Transactional
    public FoodResponse updateFood(Long id, FoodRequest request) {
        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "Food not found."));

        FoodCategory category = getCategory(request.categoryId());

        food.setCategory(category);
        food.setDishName(request.dishName().trim());
        food.setWeightG(request.weightG());
        food.setCalories(request.calories());
        food.setProteinG(request.proteinG());
        food.setFiberG(request.fiberG());
        food.setImageUrl(request.imageUrl() != null && !request.imageUrl().isBlank() ? request.imageUrl().trim() : null);
        food.setOthers(request.others() != null && !request.others().isBlank() ? request.others().trim() : null);

        Food updated = foodRepository.save(food);
        return toResponse(updated);
    }

    @Transactional
    public void deleteFood(Long id) {
        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "Food not found."));

        if (planEntryRepository.existsByFoodId(id)) {
            throw new AppException(HttpStatus.CONFLICT, ErrorCode.FOOD_IN_USE, "This food is used in a plan and cannot be deleted.");
        }

        foodRepository.delete(food);
    }

    private FoodCategory getCategory(Integer categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "Category not found."));
    }

    public FoodResponse toResponse(Food f) {
        return new FoodResponse(
                f.getId(),
                new CategoryDto(f.getCategory().getId(), f.getCategory().getName()),
                f.getDishName(),
                f.getWeightG(),
                f.getCalories(),
                f.getProteinG(),
                f.getFiberG(),
                f.getImageUrl(),
                f.getOthers(),
                f.getCreatedAt(),
                f.getUpdatedAt()
        );
    }
}

