package com.developersfab.Dhanushappbe.plan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FoodPlanEntryRepository extends JpaRepository<FoodPlanEntry, Long> {

    boolean existsByFoodId(Long foodId);

    Optional<FoodPlanEntry> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT e FROM FoodPlanEntry e " +
            "JOIN FETCH e.food f " +
            "JOIN FETCH f.category c " +
            "WHERE e.user.id = :userId " +
            "ORDER BY e.dayOfWeek ASC, e.position ASC, e.id ASC")
    List<FoodPlanEntry> findByUserIdOrderByDayAndPosition(@Param("userId") Long userId);

    @Query("SELECT e FROM FoodPlanEntry e " +
            "JOIN FETCH e.food f " +
            "JOIN FETCH f.category c " +
            "WHERE e.user.id = :userId AND e.dayOfWeek = :day " +
            "ORDER BY e.position ASC, e.id ASC")
    List<FoodPlanEntry> findByUserIdAndDayOfWeekOrderByPosition(@Param("userId") Long userId, @Param("day") Integer day);

    @Query("SELECT COALESCE(MAX(e.position), 0) FROM FoodPlanEntry e WHERE e.user.id = :userId AND e.dayOfWeek = :day")
    int findMaxPositionByUserIdAndDayOfWeek(@Param("userId") Long userId, @Param("day") Integer day);
}

