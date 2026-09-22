package com.example.service.impl;

import com.example.jpaRepository.HabitDailyLogRepository;
import com.example.model.Habit;
import com.example.model.HabitDailyLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class HabitDailyLogService {
    private final HabitDailyLogRepository logRepository;
    private final HabitService habitService;

    // 1. Mark habit as done TODAY (or toggle)
    public HabitDailyLog toggleTodaysCompletion(Long habitId, boolean completed) {
        Habit habit = habitService.getHabitById(habitId);

        LocalDate today = LocalDate.now();

        return logForHabitAndDate(habit, today, completed);
    }

    // 2. Mark habit for ANY date (for catching up)
    public HabitDailyLog logForDate(Long habitId, LocalDate date, boolean completed) {
        Habit habit = habitService.getHabitById(habitId);

        return logForHabitAndDate(habit, date, completed);
    }

    // Helper: Create or update log for specific date
    private HabitDailyLog logForHabitAndDate(Habit habit, LocalDate date, boolean completed) {
        Optional<HabitDailyLog> existingLog = logRepository.findByHabitIdAndDate(habit.getId(), date);

        if (existingLog.isPresent()) {
            HabitDailyLog log = existingLog.get();
            log.setCompleted(completed);
            return logRepository.save(log);
        } else {
            HabitDailyLog newLog = new HabitDailyLog();
            newLog.setHabit(habit);
            newLog.setDate(date);
            newLog.setCompleted(completed);
            return logRepository.save(newLog);
        }
    }

    // 3. Get today's status for a habit
    public Optional<HabitDailyLog> getTodaysLog(Long habitId) {
        return logRepository.findByHabitIdAndDate(habitId, LocalDate.now());
    }

    // 4. See past completions (calendar view - last 30 days)
    public Map<LocalDate, Boolean> getLast30DaysCompletion(Long habitId) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(29); // Last 30 days inclusive

        List<HabitDailyLog> logs = logRepository.findByHabitIdAndDateBetweenOrderByDateAsc(
                habitId, startDate, today);

        // Create map of date -> completed status
        Map<LocalDate, Boolean> completionMap = new LinkedHashMap<>();

        // Initialize all dates as false (not completed)
        for (int i = 0; i < 30; i++) {
            LocalDate date = startDate.plusDays(i);
            completionMap.put(date, false);
        }

        // Fill in actual completions
        for (HabitDailyLog log : logs) {
            completionMap.put(log.getDate(), log.isCompleted());
        }

        return completionMap;
    }

    // 5. Track streaks (current consecutive days)
    public int getCurrentStreak(Long habitId) {
        LocalDate today = LocalDate.now();
        List<HabitDailyLog> recentLogs = logRepository.findRecentCompletedLogs(habitId, today);

        int streak = 0;
        LocalDate expectedDate = today;

        for (HabitDailyLog log : recentLogs) {
            if (log.getDate().equals(expectedDate) ||
                    (log.getDate().equals(expectedDate.minusDays(1)) && streak == 0)) {

                if (log.isCompleted()) {
                    streak++;
                    expectedDate = expectedDate.minusDays(1);
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        return streak;
    }

    // 6. Get statistics (% completion rate for current week)
    public Map<String, Object> getWeeklyStats(Long habitId) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        List<HabitDailyLog> weeklyLogs = logRepository.findByHabitIdAndDateBetweenOrderByDateAsc(
                habitId, startOfWeek, endOfWeek);

        long completedCount = weeklyLogs.stream()
                .filter(HabitDailyLog::isCompleted)
                .count();

        double completionRate = (weeklyLogs.isEmpty()) ? 0.0 :
                (double) completedCount / 7 * 100;

        Map<String, Object> stats = new HashMap<>();
        stats.put("weekStart", startOfWeek);
        stats.put("weekEnd", endOfWeek);
        stats.put("completedDays", completedCount);
        stats.put("totalDays", 7);
        stats.put("completionRate", Math.round(completionRate * 100.0) / 100.0);
        stats.put("currentStreak", getCurrentStreak(habitId));

        return stats;
    }

    // 7. Weekly view (Monday-Sunday grid with completion status)
    public List<Map<String, Object>> getWeeklyView(Long habitId) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        List<Map<String, Object>> weekGrid = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate date = startOfWeek.plusDays(i);
            Optional<HabitDailyLog> log = logRepository.findByHabitIdAndDate(habitId, date);

            Map<String, Object> dayInfo = new HashMap<>();
            dayInfo.put("date", date);
            dayInfo.put("dayOfWeek", date.getDayOfWeek().toString());
            dayInfo.put("isToday", date.equals(today));
            dayInfo.put("completed", log.map(HabitDailyLog::isCompleted).orElse(false));
            dayInfo.put("hasEntry", log.isPresent());

            weekGrid.add(dayInfo);
        }

        return weekGrid;
    }

    // 8. Get all logs for a habit (for admin/view)
    public List<HabitDailyLog> getAllLogsForHabit(Long habitId) {
        return logRepository.findByHabitIdAndDateBetweenOrderByDateAsc(
                habitId, LocalDate.of(2000, 1, 1), LocalDate.of(2100, 1, 1));
    }

    // 9. Delete a specific log entry
    public void deleteLog(Long logId) {
        logRepository.deleteById(logId);
    }

    // 10. Get completion rate for specific date range
    public double getCompletionRate(Long habitId, LocalDate startDate, LocalDate endDate) {
        long totalDays = startDate.datesUntil(endDate.plusDays(1)).count();
        long completedCount = logRepository.countCompletedInDateRange(habitId, startDate, endDate);

        return totalDays > 0 ? (double) completedCount / totalDays * 100 : 0.0;
    }
}