package com.example.dto;

public record MoodPattern(
        String type,      // HABIT_MOOD, FACTOR_MOOD, HABIT_FACTOR, JOURNAL_MOOD
        String sentence,  // ready-made description for the prompt
        double strength   // 0..1, used to rank patterns
) {}