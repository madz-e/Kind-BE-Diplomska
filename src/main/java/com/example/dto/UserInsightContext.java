package com.example.dto;

import java.util.List;
import java.util.Map;

public record UserInsightContext(
        String firstName,
        Double averageMoodLast7Days,
        Double averageMoodLast14Days,
        List<Integer> moodValuesLast7Days,
        List<Double> dailyAveragesLast7Days,
        List<String> recentTexts,
        List<String> recentEmotions,
        List<String> recentFactors,
        Map<String, Double> habitCompletionRates,
        String latestNote,
        List<MoodPattern> patterns
) {}