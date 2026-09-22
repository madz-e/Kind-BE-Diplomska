package com.example.service;

import com.example.model.MoodEntry;
import com.example.model.enumerations.MoodCategory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MoodEntryService {
    MoodEntry createMoodEntry(MoodEntry moodEntry);

    Optional<MoodEntry> findById(Long id);

    List<MoodEntry> findByUserId(Long userId);

    Optional<MoodEntry> findByUserIdAndDate(Long userId, LocalDate date);

    List<MoodEntry> findByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate);

    List<MoodEntry> findByUserIdAndMoodCategory(Long userId, MoodCategory category);

    List<MoodEntry> findByUserIdAndYearMonth(Long userId, int year, int month);

    MoodEntry updateMoodEntry(Long id, MoodEntry moodEntry);

    void deleteMoodEntry(Long id);

    Double calculateAverageMood(Long userId, LocalDate startDate, LocalDate endDate);
}