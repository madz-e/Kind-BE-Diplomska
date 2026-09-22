package com.example.dto;

public record RuleBasedInsight(
        String severity,
        String moodTrend,
        boolean requiresCrisisResources,
        String reasonCode
) {}
