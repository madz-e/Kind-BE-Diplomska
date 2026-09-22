package com.example.controller;

import com.example.model.BreathingSession;
import com.example.model.User;
import com.example.service.BreathingSessionService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BreathingSessionController.class)
public class BreathingSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BreathingSessionService breathingSessionService;

    private ObjectMapper objectMapper;
    private BreathingSession testSession;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        User user = new User();
        user.setId(1L);

        testSession = new BreathingSession();
        testSession.setId(1L);
        testSession.setUser(user);
        testSession.setDate(LocalDate.now());
        testSession.setExerciseType("box");
        testSession.setCycles(4);
    }

    @Test
    public void testGetUserBreathingSessions_ReturnsOk() throws Exception {
        when(breathingSessionService.findByUserId(1L)).thenReturn(List.of(testSession));

        mockMvc.perform(get("/api/breathing-sessions/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    public void testCreateBreathingSession_Success_Returns201() throws Exception {
        when(breathingSessionService.createBreathingSession(any(BreathingSession.class))).thenReturn(testSession);

        mockMvc.perform(post("/api/breathing-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testSession)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.exerciseType").value("box"));
    }

    @Test
    public void testCreateBreathingSession_InvalidUser_Returns400() throws Exception {
        when(breathingSessionService.createBreathingSession(any(BreathingSession.class)))
                .thenThrow(new IllegalArgumentException("User not found"));

        mockMvc.perform(post("/api/breathing-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testSession)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateBreathingSession_InvalidCycles_Returns400() throws Exception {
        when(breathingSessionService.createBreathingSession(any(BreathingSession.class)))
                .thenThrow(new IllegalArgumentException("Cycles must be at least 1"));

        mockMvc.perform(post("/api/breathing-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testSession)))
                .andExpect(status().isBadRequest());
    }
}
