package com.example.service.impl;

import com.example.jpaRepository.MindfulnessExerciseRepository;
import com.example.model.CalmingSound;
import com.example.model.MindfulnessExercise;
import com.example.model.enumerations.ExerciseType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MindfulnessExerciseServiceTest {

    @Mock
    private MindfulnessExerciseRepository exerciseRepository;

    @Mock
    private CalmingSoundService soundService;

    @InjectMocks
    private MindfulnessExerciseService exerciseService;

    private MindfulnessExercise breathingExercise;
    private MindfulnessExercise meditationExercise;
    private CalmingSound testSound;

    @BeforeEach
    public void setup() {
        breathingExercise = new MindfulnessExercise();
        breathingExercise.setId(1L);
        breathingExercise.setTitle("Box Breathing");
        breathingExercise.setType(ExerciseType.BREATHING);
        breathingExercise.setDescription("4-4-4-4 pattern: Inhale 4s, Hold 4s, Exhale 4s, Pause 4s.");
        breathingExercise.setTimerDurationSeconds(300);
        breathingExercise.setAnimationIdentifier("box_breathing");

        meditationExercise = new MindfulnessExercise();
        meditationExercise.setId(2L);
        meditationExercise.setTitle("5-Minute Mindfulness");
        meditationExercise.setType(ExerciseType.MEDITATION);
        meditationExercise.setTimerDurationSeconds(300);

        testSound = new CalmingSound();
        testSound.setId(1L);
        testSound.setName("Gentle Rain");
        testSound.setAudioUrl("/sounds/rain.mp3");
        testSound.setLoopEnabled(true);
    }

    @Test
    public void testGetAllExercises_ReturnsList() {
        when(exerciseRepository.findAll()).thenReturn(List.of(breathingExercise, meditationExercise));

        List<MindfulnessExercise> result = exerciseService.getAllExercises();

        assertEquals(2, result.size());
    }

    @Test
    public void testGetExercisesByType_Breathing() {
        when(exerciseRepository.findByType(ExerciseType.BREATHING)).thenReturn(List.of(breathingExercise));

        List<MindfulnessExercise> result = exerciseService.getExercisesByType(ExerciseType.BREATHING);

        assertEquals(1, result.size());
        assertEquals(ExerciseType.BREATHING, result.get(0).getType());
    }

    @Test
    public void testGetExerciseDetails_BreathingExercise_IncludesPattern() {
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(breathingExercise));
        when(soundService.getAllCalmingSounds()).thenReturn(List.of(testSound));

        Map<String, Object> details = exerciseService.getExerciseDetails(1L);

        assertNotNull(details);
        assertEquals("Box Breathing", details.get("title"));
        assertEquals(ExerciseType.BREATHING, details.get("type"));
        assertTrue((Boolean) details.get("hasAnimation"));
        assertNotNull(details.get("breathingPattern"));
    }

    @Test
    public void testGetExerciseDetails_MeditationExercise_NoAnimation() {
        when(exerciseRepository.findById(2L)).thenReturn(Optional.of(meditationExercise));
        when(soundService.getAllCalmingSounds()).thenReturn(List.of());

        Map<String, Object> details = exerciseService.getExerciseDetails(2L);

        assertFalse((Boolean) details.get("hasAnimation"));
        assertNull(details.get("breathingPattern"));
    }

    @Test
    public void testGetExerciseDetails_NotFound_ThrowsException() {
        when(exerciseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> exerciseService.getExerciseDetails(999L));
    }

    @Test
    public void testAssignSoundToExercise_Success() {
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(breathingExercise));
        when(soundService.getCalmingSoundById(1L)).thenReturn(testSound);
        when(exerciseRepository.save(any(MindfulnessExercise.class))).thenAnswer(i -> i.getArgument(0));

        MindfulnessExercise result = exerciseService.assignSoundToExercise(1L, 1L);

        assertNotNull(result.getSound());
        assertEquals("Gentle Rain", result.getSound().getName());
    }

    @Test
    public void testAssignSoundToExercise_NullSoundId_RemovesSound() {
        breathingExercise.setSound(testSound);
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(breathingExercise));
        when(exerciseRepository.save(any(MindfulnessExercise.class))).thenAnswer(i -> i.getArgument(0));

        MindfulnessExercise result = exerciseService.assignSoundToExercise(1L, null);

        assertNull(result.getSound());
    }

    @Test
    public void testSetExerciseDuration_Success() {
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(breathingExercise));
        when(exerciseRepository.save(any(MindfulnessExercise.class))).thenAnswer(i -> i.getArgument(0));

        MindfulnessExercise result = exerciseService.setExerciseDuration(1L, 600);

        assertEquals(600, result.getTimerDurationSeconds());
    }

    @Test
    public void testStartExerciseSession_UsesCustomDuration() {
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(breathingExercise));

        Map<String, Object> session = exerciseService.startExerciseSession(1L, null, 120);

        assertEquals(120, session.get("duration"));
        assertNotNull(session.get("sessionId"));
        assertNotNull(session.get("startTime"));
    }

    @Test
    public void testStartExerciseSession_UsesDefaultDurationWhenNull() {
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(breathingExercise));

        Map<String, Object> session = exerciseService.startExerciseSession(1L, null, null);

        assertEquals(300, session.get("duration")); // exercise default
    }

    @Test
    public void testInitializeDefaultExercises_OnlyWhenEmpty() {
        when(exerciseRepository.count()).thenReturn(0L);
        when(exerciseRepository.save(any(MindfulnessExercise.class))).thenAnswer(i -> i.getArgument(0));

        exerciseService.initializeDefaultExercises();

        verify(exerciseRepository, times(6)).save(any(MindfulnessExercise.class)); // 4 breathing + 2 meditation
    }

    @Test
    public void testInitializeDefaultExercises_SkipsIfAlreadyExists() {
        when(exerciseRepository.count()).thenReturn(6L);

        exerciseService.initializeDefaultExercises();

        verify(exerciseRepository, never()).save(any());
    }
}
