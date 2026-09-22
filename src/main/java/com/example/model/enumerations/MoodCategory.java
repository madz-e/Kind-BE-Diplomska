package com.example.model.enumerations;

import lombok.Getter;

@Getter
public enum MoodCategory {
    VERY_UNPLEASANT(1, 2, "#9333EA", "Check on them"),
    UNPLEASANT(3, 4, "#3B82F6", "They're having a tough time"),
    NEUTRAL(5, 6, "#6B7280", "They're doing okay"),
    PLEASANT(7, 8, "#10B981", "They're doing well"),
    VERY_PLEASANT(9, 10, "#059669", "They're feeling great!");

    private final int minValue;
    private final int maxValue;
    private final String colorHex;
    private final String statusMessage;

    MoodCategory(int minValue, int maxValue, String colorHex, String statusMessage) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.colorHex = colorHex;
        this.statusMessage = statusMessage;
    }

    public static MoodCategory fromValue(int moodValue) {
        for (MoodCategory category : values()) {
            if (moodValue >= category.minValue && moodValue <= category.maxValue) {
                return category;
            }
        }
        throw new IllegalArgumentException("Invalid mood value: " + moodValue + ". Must be between 1 and 10.");
    }
}