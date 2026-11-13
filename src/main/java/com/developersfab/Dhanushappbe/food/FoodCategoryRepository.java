package com.developersfab.Dhanushappbe.food;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodCategoryRepository extends JpaRepository<FoodCategory, Integer> {

    List<FoodCategory> findAllByOrderByIdAsc();
}

