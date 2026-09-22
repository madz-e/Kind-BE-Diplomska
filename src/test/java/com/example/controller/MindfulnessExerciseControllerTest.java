package com.example.controller;

import com.example.model.MindfulnessExercise;
import com.example.model.enumerations.ExerciseType;
import com.example.service.impl.MindfulnessExerciseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MindfulnessExerciseController.class)
public class MindfulnessExerciseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MindfulnessExerciseService exerciseService;

    private MindfulnessExercise testExercise;

    @BeforeEach
    public void setup() {
        testExercise = new MindfulnessExercise();
        testExercise.setId(1L);
        testExercise.setTitle("Box Breathing");
        testExercise.setType(ExerciseType.BREATHING);
        testExercise.setTimerDurationSeconds(300);
    }

    @Test
    public void testGetAllExercises_ReturnsOk() throws Exception {
        when(exerciseService.getAllExercises()).thenReturn(List.of(testExercise));

        mockMvc.perform(get("/api/exercises"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Box Breathing"));
    }

    @Test
    public void testGetExercisesByType_ReturnsOk() throws Exception {
        when(exerciseService.getExercisesByType(ExerciseType.BREATHING)).thenReturn(List.of(testExercise));

        mockMvc.perform(get("/api/exercises/type/BREATHING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("BREATHING"));
    }

    @Test
    public void testGetExerciseDetails_ReturnsOk() throws Exception {
        Map<String, Object> details = Map.of(
                "id", 1L,
                "title", "Box Breathing",
                "type", "BREATHING",
                "hasAnimation", true
        );
        when(exerciseService.getExerciseDetails(1L)).thenReturn(details);

        mockMvc.perform(get("/api/exercises/1/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Box Breathing"))
                .andExpect(jsonPath("$.hasAnimation").value(true));
    }

    @Test
    public void testStartExercise_ReturnsOk() throws Exception {
        Map<String, Object> session = Map.of(
                "exerciseId", 1L,
                "duration", 300,
                "sessionId", "abc-123"
        );
        when(exerciseService.startExerciseSession(1L, null, null)).thenReturn(session);

        mockMvc.perform(post("/api/exercises/1/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value("abc-123"));
    }

    @Test
    public void testAssignSound_ReturnsOk() throws Exception {
        when(exerciseService.assignSoundToExercise(1L, 1L)).thenReturn(testExercise);

        mockMvc.perform(put("/api/exercises/1/sound")
                        .param("soundId", "1"))
                .andExpect(status().isOk());
    }

    @Test
    public void testSetDuration_ReturnsOk() throws Exception {
        when(exerciseService.setExerciseDuration(1L, 600)).thenReturn(testExercise);

        mockMvc.perform(put("/api/exercises/1/duration")
                        .param("durationSeconds", "600"))
                .andExpect(status().isOk());
    }

    @Test
    public void testInitializeExercises_ReturnsOk() throws Exception {
        doNothing().when(exerciseService).initializeDefaultExercises();

        mockMvc.perform(post("/api/exercises/initialize"))
                .andExpect(status().isOk());

        verify(exerciseService, times(1)).initializeDefaultExercises();
    }
}
