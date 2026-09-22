package com.example.service.impl;

import com.example.jpaRepository.HabitDailyLogRepository;
import com.example.model.Habit;
import com.example.model.HabitDailyLog;
import com.example.model.User;
import com.example.service.impl.HabitDailyLogService;
import com.example.service.impl.HabitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HabitDailyLogServiceTest {

    @Mock
    private HabitDailyLogRepository logRepository;

    @Mock
    private HabitService habitService;

    @InjectMocks
    private HabitDailyLogService habitDailyLogService;

    private Habit testHabit;
    private HabitDailyLog testLog;

    @BeforeEach
    public void setup() {
        testHabit = new Habit();
        testHabit.setId(1L);
        testHabit.setName("Morning Meditation");
        testHabit.setCreationDate(LocalDate.now().minusDays(10));

        testLog = new HabitDailyLog();
        testLog.setId(1L);
        testLog.setHabit(testHabit);
        testLog.setDate(LocalDate.now());
        testLog.setCompleted(true);
    }

    @Test
    public void testToggleTodaysCompletion_CreatesNewLog() {
        when(habitService.getHabitById(1L)).thenReturn(testHabit);
        when(logRepository.findByHabitIdAndDate(1L, LocalDate.now())).thenReturn(Optional.empty());
        when(logRepository.save(any(HabitDailyLog.class))).thenAnswer(i -> i.getArgument(0));

        HabitDailyLog result = habitDailyLogService.toggleTodaysCompletion(1L, true);

        assertNotNull(result);
        assertTrue(result.isCompleted());
        assertEquals(LocalDate.now(), result.getDate());
        verify(logRepository, times(1)).save(any(HabitDailyLog.class));
    }

    @Test
    public void testToggleTodaysCompletion_UpdatesExistingLog() {
        testLog.setCompleted(true);
        when(habitService.getHabitById(1L)).thenReturn(testHabit);
        when(logRepository.findByHabitIdAndDate(1L, LocalDate.now())).thenReturn(Optional.of(testLog));
        when(logRepository.save(any(HabitDailyLog.class))).thenAnswer(i -> i.getArgument(0));

        HabitDailyLog result = habitDailyLogService.toggleTodaysCompletion(1L, false);

        assertFalse(result.isCompleted());
        verify(logRepository, times(1)).save(testLog);
    }

    @Test
    public void testLogForDate_Success() {
        LocalDate pastDate = LocalDate.now().minusDays(3);
        when(habitService.getHabitById(1L)).thenReturn(testHabit);
        when(logRepository.findByHabitIdAndDate(1L, pastDate)).thenReturn(Optional.empty());
        when(logRepository.save(any(HabitDailyLog.class))).thenAnswer(i -> i.getArgument(0));

        HabitDailyLog result = habitDailyLogService.logForDate(1L, pastDate, true);

        assertNotNull(result);
        assertEquals(pastDate, result.getDate());
        assertTrue(result.isCompleted());
    }

    @Test
    public void testGetTodaysLog_ReturnsLog() {
        when(logRepository.findByHabitIdAndDate(1L, LocalDate.now())).thenReturn(Optional.of(testLog));

        Optional<HabitDailyLog> result = habitDailyLogService.getTodaysLog(1L);

        assertTrue(result.isPresent());
        assertTrue(result.get().isCompleted());
    }

    @Test
    public void testGetTodaysLog_NoLogYet_ReturnsEmpty() {
        when(logRepository.findByHabitIdAndDate(1L, LocalDate.now())).thenReturn(Optional.empty());

        Optional<HabitDailyLog> result = habitDailyLogService.getTodaysLog(1L);

        assertFalse(result.isPresent());
    }

    @Test
    public void testGetLast30DaysCompletion_ReturnsMapOf30Days() {
        when(logRepository.findByHabitIdAndDateBetweenOrderByDateAsc(
                eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(testLog));

        Map<LocalDate, Boolean> result = habitDailyLogService.getLast30DaysCompletion(1L);

        assertEquals(30, result.size());
        assertTrue(result.get(LocalDate.now()));
    }

    @Test
    public void testGetLast30DaysCompletion_UnloggedDaysAreFalse() {
        when(logRepository.findByHabitIdAndDateBetweenOrderByDateAsc(
                eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        Map<LocalDate, Boolean> result = habitDailyLogService.getLast30DaysCompletion(1L);

        assertEquals(30, result.size());
        result.values().forEach(v -> assertFalse(v));
    }

    @Test
    public void testGetCurrentStreak_NoLogs_ReturnsZero() {
        when(logRepository.findRecentCompletedLogs(eq(1L), any(LocalDate.class)))
                .thenReturn(List.of());

        int streak = habitDailyLogService.getCurrentStreak(1L);

        assertEquals(0, streak);
    }

    @Test
    public void testGetCurrentStreak_ConsecutiveDays() {
        HabitDailyLog log1 = new HabitDailyLog();
        log1.setDate(LocalDate.now());
        log1.setCompleted(true);

        HabitDailyLog log2 = new HabitDailyLog();
        log2.setDate(LocalDate.now().minusDays(1));
        log2.setCompleted(true);

        HabitDailyLog log3 = new HabitDailyLog();
        log3.setDate(LocalDate.now().minusDays(2));
        log3.setCompleted(true);

        when(logRepository.findRecentCompletedLogs(eq(1L), any(LocalDate.class)))
                .thenReturn(List.of(log1, log2, log3));

        int streak = habitDailyLogService.getCurrentStreak(1L);

        assertEquals(3, streak);
    }

    @Test
    public void testGetWeeklyStats_ReturnsCorrectFields() {
        when(logRepository.findByHabitIdAndDateBetweenOrderByDateAsc(
                eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(testLog));
        when(logRepository.findRecentCompletedLogs(eq(1L), any(LocalDate.class)))
                .thenReturn(List.of(testLog));

        Map<String, Object> stats = habitDailyLogService.getWeeklyStats(1L);

        assertNotNull(stats);
        assertTrue(stats.containsKey("weekStart"));
        assertTrue(stats.containsKey("weekEnd"));
        assertTrue(stats.containsKey("completedDays"));
        assertTrue(stats.containsKey("completionRate"));
        assertTrue(stats.containsKey("currentStreak"));
        assertEquals(7, stats.get("totalDays"));
    }

    @Test
    public void testDeleteLog_Success() {
        doNothing().when(logRepository).deleteById(1L);

        habitDailyLogService.deleteLog(1L);

        verify(logRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testGetCompletionRate_AllCompleted() {
        LocalDate start = LocalDate.now().minusDays(6);
        LocalDate end = LocalDate.now();

        when(logRepository.countCompletedInDateRange(1L, start, end)).thenReturn(7L);

        double rate = habitDailyLogService.getCompletionRate(1L, start, end);

        assertEquals(100.0, rate);
    }

    @Test
    public void testGetCompletionRate_NoneCompleted() {
        LocalDate start = LocalDate.now().minusDays(6);
        LocalDate end = LocalDate.now();

        when(logRepository.countCompletedInDateRange(1L, start, end)).thenReturn(0L);

        double rate = habitDailyLogService.getCompletionRate(1L, start, end);

        assertEquals(0.0, rate);
    }
}