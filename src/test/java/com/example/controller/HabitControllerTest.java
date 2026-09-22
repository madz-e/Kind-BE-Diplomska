package com.example.controller;

import com.example.model.Habit;
import com.example.model.User;
import com.example.model.exceptions.HabitNotFoundException;
import com.example.service.impl.HabitService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HabitController.class)
public class HabitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HabitService habitService;

    private ObjectMapper objectMapper;
    private Habit testHabit;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        User testUser = new User();
        testUser.setId(1L);

        testHabit = new Habit();
        testHabit.setId(1L);
        testHabit.setName("Morning Meditation");
        testHabit.setDescription("5 min mindfulness");
        testHabit.setColorHex("#4CAF50");
        testHabit.setIconIdentifier("meditation");
        testHabit.setCreationDate(LocalDate.now());
        testHabit.setIsSystemHabit(false);
        testHabit.setUser(testUser);
    }

    @Test
    public void testCreateHabit_ReturnsOk() throws Exception {
        when(habitService.createHabit(eq(1L), any(Habit.class))).thenReturn(testHabit);

        mockMvc.perform(post("/api/habits/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testHabit)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Morning Meditation"));
    }

    @Test
    public void testGetAllHabits_ReturnsOk() throws Exception {
        when(habitService.getAllHabits(1L)).thenReturn(List.of(testHabit));

        mockMvc.perform(get("/api/habits/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Morning Meditation"));
    }

    @Test
    public void testGetAllHabits_EmptyList_ReturnsOk() throws Exception {
        when(habitService.getAllHabits(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/habits/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void testGetHabitById_ReturnsOk() throws Exception {
        when(habitService.getHabitById(1L)).thenReturn(testHabit);

        mockMvc.perform(get("/api/habits/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Morning Meditation"));
    }

    @Test
    public void testGetHabitById_NotFound_Returns500() throws Exception {
        when(habitService.getHabitById(999L)).thenThrow(new HabitNotFoundException(999L));

        mockMvc.perform(get("/api/habits/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetHabitInfo_ReturnsStats() throws Exception {
        Map<String, Object> stats = Map.of(
                "id", 1L,
                "name", "Morning Meditation",
                "createdDate", LocalDate.now().toString(),
                "daysActive", 0L,
                "isSystemHabit", false
        );
        when(habitService.getHabitStatistics(1L)).thenReturn(stats);

        mockMvc.perform(get("/api/habits/1/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Morning Meditation"))
                .andExpect(jsonPath("$.isSystemHabit").value(false));
    }

    @Test
    public void testDeleteHabit_ReturnsNoContent() throws Exception {
        doNothing().when(habitService).deleteHabit(1L);

        mockMvc.perform(delete("/api/habits/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testAddDefaultHabits_ReturnsOk() throws Exception {
        doNothing().when(habitService).addDefaultHabits(1L);

        mockMvc.perform(post("/api/habits/user/1/add-defaults"))
                .andExpect(status().isOk());

        verify(habitService, times(1)).addDefaultHabits(1L);
    }
}