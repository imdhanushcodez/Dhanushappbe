package com.developersfab.Dhanushappbe.plan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkoutPlanEntryRepository extends JpaRepository<WorkoutPlanEntry, Long> {

    boolean existsByWorkoutId(Long workoutId);

    Optional<WorkoutPlanEntry> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT e FROM WorkoutPlanEntry e " +
            "JOIN FETCH e.workout w " +
            "JOIN FETCH w.category c " +
            "WHERE e.user.id = :userId " +
            "ORDER BY e.dayOfWeek ASC, e.position ASC, e.id ASC")
    List<WorkoutPlanEntry> findByUserIdOrderByDayAndPosition(@Param("userId") Long userId);

    @Query("SELECT e FROM WorkoutPlanEntry e " +
            "JOIN FETCH e.workout w " +
            "JOIN FETCH w.category c " +
            "WHERE e.user.id = :userId AND e.dayOfWeek = :day " +
            "ORDER BY e.position ASC, e.id ASC")
    List<WorkoutPlanEntry> findByUserIdAndDayOfWeekOrderByPosition(@Param("userId") Long userId, @Param("day") Integer day);

    @Query("SELECT COALESCE(MAX(e.position), 0) FROM WorkoutPlanEntry e WHERE e.user.id = :userId AND e.dayOfWeek = :day")
    int findMaxPositionByUserIdAndDayOfWeek(@Param("userId") Long userId, @Param("day") Integer day);
}

