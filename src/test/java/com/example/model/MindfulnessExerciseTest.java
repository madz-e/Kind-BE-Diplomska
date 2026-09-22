package com.example.model;

import com.example.model.enumerations.ExerciseType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MindfulnessExerciseTest {

    @Test
    public void testBreathingExerciseCreation() {
        MindfulnessExercise exercise = new MindfulnessExercise();
        exercise.setTitle("Box Breathing");
        exercise.setType(ExerciseType.BREATHING);
        exercise.setDescription("4-4-4-4 pattern");
        exercise.setTimerDurationSeconds(300);
        exercise.setAnimationIdentifier("box_breathing");

        assertEquals("Box Breathing", exercise.getTitle());
        assertEquals(ExerciseType.BREATHING, exercise.getType());
        assertEquals(300, exercise.getTimerDurationSeconds());
        assertNull(exercise.getSound());
    }

    @Test
    public void testMeditationExercise_NoAnimation() {
        MindfulnessExercise exercise = new MindfulnessExercise();
        exercise.setTitle("5-Minute Mindfulness");
        exercise.setType(ExerciseType.MEDITATION);
        exercise.setTimerDurationSeconds(300);
        exercise.setAnimationIdentifier(null);

        assertEquals(ExerciseType.MEDITATION, exercise.getType());
        assertNull(exercise.getAnimationIdentifier());
    }

    @Test
    public void testExerciseWithSound() {
        CalmingSound sound = new CalmingSound();
        sound.setId(1L);
        sound.setName("Gentle Rain");
        sound.setLoopEnabled(true);

        MindfulnessExercise exercise = new MindfulnessExercise();
        exercise.setTitle("Meditation with Rain");
        exercise.setType(ExerciseType.MEDITATION);
        exercise.setSound(sound);

        assertNotNull(exercise.getSound());
        assertEquals("Gentle Rain", exercise.getSound().getName());
    }
}
