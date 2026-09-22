package com.example.model;

import com.example.model.enumerations.MoodCategory;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MoodEntryTest {

    @Test
    public void testSetMoodValue_AutoSetsCategory() {
        MoodEntry entry = new MoodEntry();
        entry.setMoodValue(3);

        assertEquals(3, entry.getMoodValue());
        assertEquals(MoodCategory.UNPLEASANT, entry.getMoodCategory());
    }

    @Test
    public void testMoodCategoryFromValue_AllRanges() {
        assertEquals(MoodCategory.VERY_UNPLEASANT, MoodCategory.fromValue(1));
        assertEquals(MoodCategory.VERY_UNPLEASANT, MoodCategory.fromValue(2));
        assertEquals(MoodCategory.UNPLEASANT, MoodCategory.fromValue(3));
        assertEquals(MoodCategory.UNPLEASANT, MoodCategory.fromValue(4));
        assertEquals(MoodCategory.NEUTRAL, MoodCategory.fromValue(5));
        assertEquals(MoodCategory.NEUTRAL, MoodCategory.fromValue(6));
        assertEquals(MoodCategory.PLEASANT, MoodCategory.fromValue(7));
        assertEquals(MoodCategory.PLEASANT, MoodCategory.fromValue(8));
        assertEquals(MoodCategory.VERY_PLEASANT, MoodCategory.fromValue(9));
        assertEquals(MoodCategory.VERY_PLEASANT, MoodCategory.fromValue(10));
    }

    @Test
    public void testMoodCategoryFromValue_InvalidValue_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> MoodCategory.fromValue(0));
        assertThrows(IllegalArgumentException.class, () -> MoodCategory.fromValue(11));
    }

    @Test
    public void testMoodEntryWithEmotionsAndFactors() {
        Emotion emotion = new Emotion();
        emotion.setId(1L);
        emotion.setName("Happy");

        MoodFactor factor = new MoodFactor();
        factor.setId(1L);
        factor.setName("Exercise");

        MoodEntry entry = new MoodEntry();
        entry.setMoodValue(8);
        entry.getSelectedEmotions().add(emotion);
        entry.getSelectedFactors().add(factor);

        assertEquals(1, entry.getSelectedEmotions().size());
        assertEquals(1, entry.getSelectedFactors().size());
        assertEquals(MoodCategory.PLEASANT, entry.getMoodCategory());
    }
}
