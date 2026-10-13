package com.example.service.impl;

import com.example.dto.RuleBasedInsight;
import com.example.dto.UserInsightContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RuleBasedInsightServiceImpl {

    private static final List<String> CRISIS_PHRASES = List.of(
            "kill myself", "suicide", "end my life", "want to die", "hurt myself"
    );

    public RuleBasedInsight evaluate(UserInsightContext ctx) {
        // 1. Check ALL recent writing (mood notes + journal), not just the latest note
        for (String text : ctx.recentTexts()) {
            String lower = text.toLowerCase();
            for (String phrase : CRISIS_PHRASES) {
                if (lower.contains(phrase)) {
                    return new RuleBasedInsight("CRISIS_FLAG", "STABLE", true, "CRISIS_KEYWORD_MATCH");
                }
            }
        }

        // 2. Streak of low DAYS (daily average ≤ 2), not low entries
        int longestStreak = 0;
        int currentStreak = 0;
        for (Double dayAvg : ctx.dailyAveragesLast7Days()) {
            if (dayAvg <= 2.0) {
                currentStreak++;
                longestStreak = Math.max(longestStreak, currentStreak);
            } else {
                currentStreak = 0;
            }
        }

        String moodTrend = determineTrend(ctx.averageMoodLast7Days(), ctx.averageMoodLast14Days());

        String severity;
        if (longestStreak >= 5) {
            severity = "ELEVATED_CONCERN";
        } else if (longestStreak >= 2) {
            severity = "MODERATE";
        } else {
            severity = "LOW_RISK";
        }

        return new RuleBasedInsight(severity, moodTrend, false, "STREAK_" + longestStreak);
    }
    private String determineTrend(Double avg7, Double avg14) {
        if (avg7 == null || avg14 == null) return "STABLE";
        double diff = avg7 - avg14;
        if (diff > 0.3) return "IMPROVING";
        if (diff < -0.3) return "DECLINING";
        return "STABLE";
    }
}
