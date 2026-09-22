package com.example.jpaRepository;

import com.example.model.HabitDailyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;   // CHANGED: needed for bulk delete
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Repository
public interface HabitDailyLogRepository extends JpaRepository<HabitDailyLog, Long> {

    // CHANGED: bulk-delete every log belonging to a habit, so a habit that has
    //          completion logs (i.e. the default habits, which users actually tick off)
    //          can be removed without hitting the habit_daily_log FK constraint → 500.
    @Modifying
    @Query("DELETE FROM HabitDailyLog h WHERE h.habit.id = :habitId")
    void deleteByHabitId(@Param("habitId") Long habitId);

    // Get log for specific habit and date
    Optional<HabitDailyLog> findByHabitIdAndDate(Long habitId, LocalDate date);

    // Get all logs for a habit in date range (weekly view: Mon-Sun)
    List<HabitDailyLog> findByHabitIdAndDateBetweenOrderByDateAsc(Long habitId,
                                                                  LocalDate startDate,
                                                                  LocalDate endDate);

    // Count completed days for completion rate
    @Query("SELECT COUNT(h) FROM HabitDailyLog h " +
            "WHERE h.habit.id = :habitId " +
            "AND h.date BETWEEN :startDate AND :endDate " +
            "AND h.completed = true")
    long countCompletedInDateRange(@Param("habitId") Long habitId,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);

    // Get recent completed logs (for streak calculation)
    @Query("SELECT h FROM HabitDailyLog h " +
            "WHERE h.habit.id = :habitId " +
            "AND h.date <= :currentDate " +
            "AND h.completed = true " +
            "ORDER BY h.date DESC")
    List<HabitDailyLog> findRecentCompletedLogs(@Param("habitId") Long habitId,
                                                @Param("currentDate") LocalDate currentDate);
}