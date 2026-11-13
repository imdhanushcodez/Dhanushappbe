package com.developersfab.Dhanushappbe.workout;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkoutRepository extends JpaRepository<Workout, Long> {

    @Query("SELECT w FROM Workout w JOIN FETCH w.category c " +
            "WHERE (:categoryId IS NULL OR c.id = :categoryId) " +
            "ORDER BY c.name ASC, w.name ASC")
    List<Workout> findAllByCategoryIdOrder(@Param("categoryId") Integer categoryId);
}

