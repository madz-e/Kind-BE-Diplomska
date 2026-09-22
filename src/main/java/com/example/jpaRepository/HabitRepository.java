package com.example.jpaRepository;

import com.example.model.Habit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HabitRepository extends JpaRepository<Habit, Long> {

    // Get all habits for a user (oldest first - creation order)
    List<Habit> findByUserIdOrderByCreationDateAsc(Long userId);

    // Get system habits vs user-created habits
    List<Habit> findByUserIdAndIsSystemHabit(Long userId, Boolean isSystemHabit);

    // Count user's habits
    long countByUserId(Long userId);
}