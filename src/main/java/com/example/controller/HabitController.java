package com.example.controller;

import com.example.model.Habit;
import com.example.service.impl.HabitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/habits")
@RequiredArgsConstructor
public class HabitController {
    private final HabitService habitService;

    // Create custom habit - KEEP
    @PostMapping("/user/{userId}")
    public ResponseEntity<Habit> createHabit(@PathVariable Long userId,
                                             @RequestBody Habit habit) {
        return ResponseEntity.ok(habitService.createHabit(userId, habit));
    }

    // Get ALL habits - KEEP
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Habit>> getAllHabits(@PathVariable Long userId) {
        return ResponseEntity.ok(habitService.getAllHabits(userId));
    }

    // Get single habit info (OPTIONAL - only if needed)
    @GetMapping("/{habitId}")
    public ResponseEntity<Habit> getHabit(@PathVariable Long habitId) {
        return ResponseEntity.ok(habitService.getHabitById(habitId));
    }

    // Get habit statistics/info (OPTIONAL)
    @GetMapping("/{habitId}/info")
    public ResponseEntity<Map<String, Object>> getHabitInfo(@PathVariable Long habitId) {
        return ResponseEntity.ok(habitService.getHabitStatistics(habitId));
    }

    // Delete habit - KEEP
    @DeleteMapping("/{habitId}")
    public ResponseEntity<Void> deleteHabit(@PathVariable Long habitId) {
        habitService.deleteHabit(habitId);
        return ResponseEntity.noContent().build();
    }

    // Add default habits - KEEP
    @PostMapping("/user/{userId}/add-defaults")
    public ResponseEntity<Void> addDefaultHabits(@PathVariable Long userId) {
        habitService.addDefaultHabits(userId);
        return ResponseEntity.ok().build();
    }
}