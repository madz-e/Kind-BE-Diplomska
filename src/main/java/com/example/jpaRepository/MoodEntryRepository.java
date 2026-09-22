package com.example.jpaRepository;

import com.example.model.MoodEntry;
import com.example.model.enumerations.MoodCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MoodEntryRepository extends JpaRepository<MoodEntry, Long> {

    // Get all mood entries for a user
    List<MoodEntry> findByUserId(Long userId);

    // Get mood entry for specific date
    Optional<MoodEntry> findByUserIdAndDate(Long userId, LocalDate date);

    // Get mood entries in date range
    List<MoodEntry> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    // Get moods by category
    List<MoodEntry> findByUserIdAndMoodCategory(Long userId, MoodCategory category);

    // Calendar view: Get all moods for a specific month
    @Query("SELECT m FROM MoodEntry m WHERE m.user.id = :userId " +
            "AND EXTRACT(YEAR FROM m.date) = :year " +
            "AND EXTRACT(MONTH FROM m.date) = :month " +
            "ORDER BY m.date ASC")
    List<MoodEntry> findByUserIdAndYearMonth(@Param("userId") Long userId,
                                             @Param("year") int year,
                                             @Param("month") int month);

    // Calculate average mood for analytics
    @Query("SELECT AVG(m.moodValue) FROM MoodEntry m " +
            "WHERE m.user.id = :userId " +
            "AND m.date BETWEEN :startDate AND :endDate")
    Double calculateAverageMood(@Param("userId") Long userId,
                                @Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate);
}