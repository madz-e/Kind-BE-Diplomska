package com.example.controller;

import com.example.model.HabitDailyLog;
import com.example.service.impl.HabitDailyLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/habits/{habitId}/logs")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000") // For React frontend
public class HabitDailyLogController {
    private final HabitDailyLogService logService;

    // 1. Toggle today's completion
    @PostMapping("/today")
    public ResponseEntity<HabitDailyLog> toggleToday(
            @PathVariable Long habitId,
            @RequestParam boolean completed) {
        return ResponseEntity.ok(logService.toggleTodaysCompletion(habitId, completed));
    }

    // 2. Log for specific date (for catching up)
    @PostMapping("/date/{date}")
    public ResponseEntity<HabitDailyLog> logForDate(
            @PathVariable Long habitId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam boolean completed) {
        return ResponseEntity.ok(logService.logForDate(habitId, date, completed));
    }

    // 3. Get today's status
    @GetMapping("/today")
    public ResponseEntity<Optional<HabitDailyLog>> getToday(@PathVariable Long habitId) {
        return ResponseEntity.ok(logService.getTodaysLog(habitId));
    }

    // 4. Get last 30 days calendar view
    @GetMapping("/calendar")
    public ResponseEntity<Map<LocalDate, Boolean>> getCalendar(@PathVariable Long habitId) {
        return ResponseEntity.ok(logService.getLast30DaysCompletion(habitId));
    }

    // 5. Get current streak
    @GetMapping("/streak")
    public ResponseEntity<Integer> getStreak(@PathVariable Long habitId) {
        return ResponseEntity.ok(logService.getCurrentStreak(habitId));
    }

    // 6. Get weekly statistics
    @GetMapping("/stats/weekly")
    public ResponseEntity<Map<String, Object>> getWeeklyStats(@PathVariable Long habitId) {
        return ResponseEntity.ok(logService.getWeeklyStats(habitId));
    }

    // 7. Get weekly grid (Monday-Sunday)
    @GetMapping("/week")
    public ResponseEntity<List<Map<String, Object>>> getWeeklyView(@PathVariable Long habitId) {
        return ResponseEntity.ok(logService.getWeeklyView(habitId));
    }

    // 8. Get all logs (optional - for debugging)
    @GetMapping("/all")
    public ResponseEntity<List<HabitDailyLog>> getAllLogs(@PathVariable Long habitId) {
        return ResponseEntity.ok(logService.getAllLogsForHabit(habitId));
    }

    // 9. Delete a specific log entry
    @DeleteMapping("/{logId}")
    public ResponseEntity<Void> deleteLog(@PathVariable Long logId) {
        logService.deleteLog(logId);
        return ResponseEntity.noContent().build();
    }

    // 10. Get completion rate for custom date range
    @GetMapping("/completion-rate")
    public ResponseEntity<Double> getCompletionRate(
            @PathVariable Long habitId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(logService.getCompletionRate(habitId, startDate, endDate));
    }
}