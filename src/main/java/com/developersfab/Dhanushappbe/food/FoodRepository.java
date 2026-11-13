package com.developersfab.Dhanushappbe.food;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {

    @Query("SELECT f FROM Food f JOIN FETCH f.category c " +
            "WHERE (:categoryId IS NULL OR c.id = :categoryId) " +
            "ORDER BY c.name ASC, f.dishName ASC")
    List<Food> findAllByCategoryIdOrder(@Param("categoryId") Integer categoryId);
}

