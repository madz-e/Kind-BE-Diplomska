package com.example.dto;

public record InsightResponse(
        String feelingSummary,
        String recommendation,
        String source,
        RuleBasedInsight ruleBasedInsight,
        String reason
) {}
