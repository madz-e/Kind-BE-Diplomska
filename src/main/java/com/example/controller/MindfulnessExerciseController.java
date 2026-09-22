package com.example.controller;

import com.example.model.MindfulnessExercise;
import com.example.model.enumerations.ExerciseType;
import com.example.service.impl.MindfulnessExerciseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class MindfulnessExerciseController {
    private final MindfulnessExerciseService exerciseService;

    // 1. Get all exercises (library view)
    @GetMapping
    public ResponseEntity<List<MindfulnessExercise>> getAllExercises() {
        return ResponseEntity.ok(exerciseService.getAllExercises());
    }

    // 2. Get exercises by type (filter: BREATHING or MEDITATION)
    @GetMapping("/type/{type}")
    public ResponseEntity<List<MindfulnessExercise>> getExercisesByType(@PathVariable ExerciseType type) {
        return ResponseEntity.ok(exerciseService.getExercisesByType(type));
    }

    // 3. Get exercise details (for starting an exercise)
    @GetMapping("/{exerciseId}/details")
    public ResponseEntity<Map<String, Object>> getExerciseDetails(@PathVariable Long exerciseId) {
        return ResponseEntity.ok(exerciseService.getExerciseDetails(exerciseId));
    }

    // 4. Start an exercise session (with optional sound and custom duration)
    @PostMapping("/{exerciseId}/start")
    public ResponseEntity<Map<String, Object>> startExercise(
            @PathVariable Long exerciseId,
            @RequestParam(required = false) Long soundId,
            @RequestParam(required = false) Integer duration) {
        return ResponseEntity.ok(exerciseService.startExerciseSession(exerciseId, soundId, duration));
    }

    // 5. Get sound options for dropdown
    @GetMapping("/sound-options")
    public ResponseEntity<List<Map<String, Object>>> getSoundOptions() {
        return ResponseEntity.ok(exerciseService.getSoundOptions());
    }

    // 6. Assign sound to exercise (save preference)
    @PutMapping("/{exerciseId}/sound")
    public ResponseEntity<MindfulnessExercise> assignSound(
            @PathVariable Long exerciseId,
            @RequestParam(required = false) Long soundId) {
        return ResponseEntity.ok(exerciseService.assignSoundToExercise(exerciseId, soundId));
    }

    // 7. Set custom duration for exercise
    @PutMapping("/{exerciseId}/duration")
    public ResponseEntity<MindfulnessExercise> setDuration(
            @PathVariable Long exerciseId,
            @RequestParam Integer durationSeconds) {
        return ResponseEntity.ok(exerciseService.setExerciseDuration(exerciseId, durationSeconds));
    }

    // 7. Initialize default exercises (call once on app start)
    @PostMapping("/initialize")
    public ResponseEntity<Void> initializeExercises() {
        exerciseService.initializeDefaultExercises();
        return ResponseEntity.ok().build();
    }
}