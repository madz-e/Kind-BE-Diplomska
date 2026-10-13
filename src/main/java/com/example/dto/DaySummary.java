package com.example.dto;

import java.util.IntSummaryStatistics;
import java.util.List;

public record DaySummary(double average, double lowest, double swing) {

    public static DaySummary of(List<Integer> moods) {
        IntSummaryStatistics s = moods.stream().mapToInt(Integer::intValue).summaryStatistics();
        return new DaySummary(s.getAverage(), s.getMin(), s.getMax() - s.getMin());
    }
}