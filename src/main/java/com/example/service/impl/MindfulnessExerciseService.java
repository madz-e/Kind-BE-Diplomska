package com.example.service.impl;

import com.example.jpaRepository.MindfulnessExerciseRepository;
import com.example.model.CalmingSound;
import com.example.model.MindfulnessExercise;
import com.example.model.enumerations.ExerciseType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class MindfulnessExerciseService {
    private final MindfulnessExerciseRepository exerciseRepository;
    private final CalmingSoundService soundService;

    // 1. Get all exercises (for library browsing)
    public List<MindfulnessExercise> getAllExercises() {
        return exerciseRepository.findAll();
    }

    // 2. Get exercises by type (BREATHING or MEDITATION)
    public List<MindfulnessExercise> getExercisesByType(ExerciseType type) {
        return exerciseRepository.findByType(type);
    }

    // 3. Get exercise with all details for frontend
    public Map<String, Object> getExerciseDetails(Long exerciseId) {
        MindfulnessExercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new RuntimeException("Exercise not found"));

        Map<String, Object> details = new HashMap<>();
        details.put("id", exercise.getId());
        details.put("title", exercise.getTitle());
        details.put("type", exercise.getType());
        details.put("description", exercise.getDescription());
        details.put("duration", exercise.getTimerDurationSeconds());
        details.put("animation", exercise.getAnimationIdentifier());

        // Add breathing pattern if it's a breathing exercise
        if (exercise.getType() == ExerciseType.BREATHING) {
            details.put("breathingPattern", extractBreathingPattern(exercise));
            details.put("hasAnimation", true);
        } else {
            details.put("hasAnimation", false);
        }

        // Add sound options
        details.put("soundOptions", getSoundOptions());
        details.put("currentSound", getCurrentSoundInfo(exercise));

        return details;
    }

    // 4. Initialize default exercises (run once)
    @Transactional
    public void initializeDefaultExercises() {
        if (exerciseRepository.count() == 0) {
            createBreathingExercises();
            createMeditationExercises();
        }
    }

    // 5. Assign sound to exercise
    public MindfulnessExercise assignSoundToExercise(Long exerciseId, Long soundId) {
        MindfulnessExercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new RuntimeException("Exercise not found"));

        if (soundId != null) {
            CalmingSound sound = soundService.getCalmingSoundById(soundId);
            exercise.setSound(sound);
        } else {
            exercise.setSound(null); // No sound
        }

        return exerciseRepository.save(exercise);
    }

    // 6. Set custom duration for an exercise
    public MindfulnessExercise setExerciseDuration(Long exerciseId, Integer durationSeconds) {
        MindfulnessExercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new RuntimeException("Exercise not found"));

        exercise.setTimerDurationSeconds(durationSeconds);
        return exerciseRepository.save(exercise);
    }

    // 6. Start exercise session (for tracking/completion)
    public Map<String, Object> startExerciseSession(Long exerciseId, Long soundId, Integer customDuration) {
        MindfulnessExercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new RuntimeException("Exercise not found"));

        Map<String, Object> session = new HashMap<>();
        session.put("exerciseId", exercise.getId());
        session.put("title", exercise.getTitle());
        session.put("type", exercise.getType());

        // Use custom duration if provided, otherwise use exercise default
        int duration = (customDuration != null) ? customDuration :
                (exercise.getTimerDurationSeconds() != null ? exercise.getTimerDurationSeconds() : 300);
        session.put("duration", duration);

        // Get sound info if selected
        if (soundId != null) {
            CalmingSound sound = soundService.getCalmingSoundById(soundId);
            if (sound != null) {
                Map<String, Object> soundMap = new HashMap<>();
                soundMap.put("id", sound.getId());
                soundMap.put("name", sound.getName() != null ? sound.getName() : "Unknown");
                soundMap.put("audioUrl", sound.getAudioUrl());
                soundMap.put("loopEnabled", sound.isLoopEnabled());
                session.put("sound", soundMap);
            }
        }

        // Add breathing pattern for breathing exercises
        if (exercise.getType() == ExerciseType.BREATHING) {
            session.put("breathingPattern", extractBreathingPattern(exercise));
            session.put("animation", exercise.getAnimationIdentifier());
        }

        session.put("startTime", new Date());
        session.put("sessionId", UUID.randomUUID().toString());

        return session;
    }

    // 7. Get available sounds for dropdown
    public List<Map<String, Object>> getSoundOptions() {
        List<CalmingSound> allSounds = soundService.getAllCalmingSounds();
        List<Map<String, Object>> options = new ArrayList<>();

        // Add "No Sound" option
        Map<String, Object> noSoundOption = new HashMap<>();
        noSoundOption.put("id", null);      // ✅ HashMap allows null
        noSoundOption.put("name", "No Sound");
        noSoundOption.put("loopEnabled", false);
        noSoundOption.put("isDefault", true);
        options.add(noSoundOption);

        // Add all available sounds
        if (allSounds != null) {
            for (CalmingSound sound : allSounds) {
                if (sound != null) {
                    Map<String, Object> soundOption = new HashMap<>();
                    soundOption.put("id", sound.getId());           // ✅ Null safe
                    soundOption.put("name", sound.getName());       // ✅ Null safe
                    soundOption.put("loopEnabled", sound.isLoopEnabled());
                    soundOption.put("isDefault", false);
                    options.add(soundOption);
                }
            }
        }

        return options;
    }

    // ===== PRIVATE HELPER METHODS =====

    private void createBreathingExercises() {
        // Box Breathing
        MindfulnessExercise boxBreathing = new MindfulnessExercise();
        boxBreathing.setTitle("Box Breathing");
        boxBreathing.setType(ExerciseType.BREATHING);
        boxBreathing.setDescription("4-4-4-4 pattern: Inhale 4s, Hold 4s, Exhale 4s, Pause 4s. Great for stress relief.");
        boxBreathing.setTimerDurationSeconds(300); // 5 minutes
        boxBreathing.setAnimationIdentifier("box_breathing");
        exerciseRepository.save(boxBreathing);

        // 4-7-8 Breathing
        MindfulnessExercise breathing478 = new MindfulnessExercise();
        breathing478.setTitle("4-7-8 Breathing");
        breathing478.setType(ExerciseType.BREATHING);
        breathing478.setDescription("Inhale 4s, Hold 7s, Exhale 8s. Helps with anxiety and sleep.");
        breathing478.setTimerDurationSeconds(380); // ~6 minutes
        breathing478.setAnimationIdentifier("breathing_478");
        exerciseRepository.save(breathing478);

        // Deep Belly Breathing
        MindfulnessExercise bellyBreathing = new MindfulnessExercise();
        bellyBreathing.setTitle("Deep Belly Breathing");
        bellyBreathing.setType(ExerciseType.BREATHING);
        bellyBreathing.setDescription("Focus on expanding your belly as you breathe deeply. Promotes relaxation.");
        bellyBreathing.setTimerDurationSeconds(240); // 4 minutes
        bellyBreathing.setAnimationIdentifier("belly_breathing");
        exerciseRepository.save(bellyBreathing);

        // HRT (Heart Rhythm Breathing)
        MindfulnessExercise hrtBreathing = new MindfulnessExercise();
        hrtBreathing.setTitle("Heart Rhythm Breathing");
        hrtBreathing.setType(ExerciseType.BREATHING);
        hrtBreathing.setDescription("Inhale 2 times sharply, exhale deeply. Sync your breath with your heart rate for coherence. Reduces stress.");
        hrtBreathing.setTimerDurationSeconds(360); // 6 minutes
        hrtBreathing.setAnimationIdentifier("hrt_breathing");
        exerciseRepository.save(hrtBreathing);
    }

    private void createMeditationExercises() {
        // 5-minute Mindfulness
        MindfulnessExercise mindfulness = new MindfulnessExercise();
        mindfulness.setTitle("5-Minute Mindfulness");
        mindfulness.setType(ExerciseType.MEDITATION);
        mindfulness.setDescription("Brief mindfulness meditation to center yourself. Focus on your breath.");
        mindfulness.setTimerDurationSeconds(300); // 5 minutes
        mindfulness.setAnimationIdentifier(null); // No animation for meditation
        exerciseRepository.save(mindfulness);

        // Body Scan Meditation
        MindfulnessExercise bodyScan = new MindfulnessExercise();
        bodyScan.setTitle("Body Scan Meditation");
        bodyScan.setType(ExerciseType.MEDITATION);
        bodyScan.setDescription("Scan through your body from head to toe, noticing sensations without judgment.");
        bodyScan.setTimerDurationSeconds(600); // 10 minutes
        bodyScan.setAnimationIdentifier(null);
        exerciseRepository.save(bodyScan);
    }

    private Map<String, Integer> extractBreathingPattern(MindfulnessExercise exercise) {
        Map<String, Integer> pattern = new HashMap<>();

        // Extract pattern from description or title
        String title = exercise.getTitle().toLowerCase();
        String desc = exercise.getDescription();

        if (title.contains("box")) {
            pattern.put("inhale", 4);
            pattern.put("hold1", 4);
            pattern.put("exhale", 4);
            pattern.put("hold2", 4);
            pattern.put("cycles", 10);
        } else if (title.contains("4-7-8") || desc.contains("4-7-8")) {
            pattern.put("inhale", 4);
            pattern.put("hold", 7);
            pattern.put("exhale", 8);
            pattern.put("cycles", 6);
        } else if (title.contains("deep") || title.contains("belly")) {
            pattern.put("inhale", 5);
            pattern.put("exhale", 5);
            pattern.put("cycles", 12);
        } else if (title.contains("hrt") || title.contains("heart")) {
            pattern.put("inhale", 2);
            pattern.put("inhale", 2);
            pattern.put("exhale", 5);
            pattern.put("cycles", 10);
        } else {
            // Default pattern
            pattern.put("inhale", 4);
            pattern.put("exhale", 4);
            pattern.put("cycles", 15);
        }

        return pattern;
    }

    private Map<String, Object> getCurrentSoundInfo(MindfulnessExercise exercise) {
        Map<String, Object> soundInfo = new HashMap<>();

        if (exercise.getSound() == null) {
            soundInfo.put("id", null);
            soundInfo.put("name", "No Sound");
            soundInfo.put("audioUrl", null);
        } else {
            soundInfo.put("id", exercise.getSound().getId());
            soundInfo.put("name", exercise.getSound().getName());
            soundInfo.put("audioUrl", exercise.getSound().getAudioUrl());
        }

        return soundInfo;
    }

}