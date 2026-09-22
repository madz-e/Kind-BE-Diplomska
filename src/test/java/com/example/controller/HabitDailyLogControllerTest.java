package com.example.controller;

import com.example.model.Habit;
import com.example.model.HabitDailyLog;
import com.example.service.impl.HabitDailyLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HabitDailyLogController.class)
public class HabitDailyLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HabitDailyLogService logService;

    private HabitDailyLog testLog;

    @BeforeEach
    public void setup() {
        Habit testHabit = new Habit();
        testHabit.setId(1L);
        testHabit.setName("Morning Meditation");

        testLog = new HabitDailyLog();
        testLog.setId(1L);
        testLog.setHabit(testHabit);
        testLog.setDate(LocalDate.now());
        testLog.setCompleted(true);
    }

    @Test
    public void testToggleToday_Completed_ReturnsOk() throws Exception {
        when(logService.toggleTodaysCompletion(1L, true)).thenReturn(testLog);

        mockMvc.perform(post("/api/habits/1/logs/today")
                        .param("completed", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    public void testToggleToday_Uncompleted_ReturnsOk() throws Exception {
        testLog.setCompleted(false);
        when(logService.toggleTodaysCompletion(1L, false)).thenReturn(testLog);

        mockMvc.perform(post("/api/habits/1/logs/today")
                        .param("completed", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    public void testLogForDate_ReturnsOk() throws Exception {
        LocalDate pastDate = LocalDate.now().minusDays(3);
        testLog.setDate(pastDate);
        when(logService.logForDate(eq(1L), eq(pastDate), eq(true))).thenReturn(testLog);

        mockMvc.perform(post("/api/habits/1/logs/date/" + pastDate)
                        .param("completed", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    public void testGetToday_LogExists_ReturnsOk() throws Exception {
        when(logService.getTodaysLog(1L)).thenReturn(Optional.of(testLog));

        mockMvc.perform(get("/api/habits/1/logs/today"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetToday_NoLog_ReturnsEmpty() throws Exception {
        when(logService.getTodaysLog(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/habits/1/logs/today"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetStreak_ReturnsOk() throws Exception {
        when(logService.getCurrentStreak(1L)).thenReturn(5);

        mockMvc.perform(get("/api/habits/1/logs/streak"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5));
    }

    @Test
    public void testGetStreak_NoStreak_ReturnsZero() throws Exception {
        when(logService.getCurrentStreak(1L)).thenReturn(0);

        mockMvc.perform(get("/api/habits/1/logs/streak"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(0));
    }

    @Test
    public void testGetWeeklyStats_ReturnsOk() throws Exception {
        Map<String, Object> stats = Map.of(
                "weekStart", LocalDate.now().toString(),
                "weekEnd", LocalDate.now().plusDays(6).toString(),
                "completedDays", 3L,
                "totalDays", 7,
                "completionRate", 42.86,
                "currentStreak", 2
        );
        when(logService.getWeeklyStats(1L)).thenReturn(stats);

        mockMvc.perform(get("/api/habits/1/logs/stats/weekly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDays").value(7))
                .andExpect(jsonPath("$.completedDays").value(3));
    }

    @Test
    public void testGetAllLogs_ReturnsOk() throws Exception {
        when(logService.getAllLogsForHabit(1L)).thenReturn(List.of(testLog));

        mockMvc.perform(get("/api/habits/1/logs/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    public void testDeleteLog_ReturnsNoContent() throws Exception {
        doNothing().when(logService).deleteLog(1L);

        mockMvc.perform(delete("/api/habits/1/logs/1"))
                .andExpect(status().isNoContent());

        verify(logService, times(1)).deleteLog(1L);
    }

    @Test
    public void testGetCompletionRate_ReturnsOk() throws Exception {
        LocalDate start = LocalDate.now().minusDays(6);
        LocalDate end = LocalDate.now();
        when(logService.getCompletionRate(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(85.71);

        mockMvc.perform(get("/api/habits/1/logs/completion-rate")
                        .param("startDate", start.toString())
                        .param("endDate", end.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(85.71));
    }
}