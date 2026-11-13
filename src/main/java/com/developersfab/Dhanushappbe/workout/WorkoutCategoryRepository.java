package com.developersfab.Dhanushappbe.workout;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkoutCategoryRepository extends JpaRepository<WorkoutCategory, Integer> {

    List<WorkoutCategory> findAllByOrderByIdAsc();
}

