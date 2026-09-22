package com.example.controller;

import com.example.model.MoodEntry;
import com.example.model.User;
import com.example.model.enumerations.MoodCategory;
import com.example.service.MoodEntryService;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MoodEntryController.class)
public class MoodEntryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MoodEntryService moodEntryService;

    private ObjectMapper objectMapper;
    private MoodEntry testEntry;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        User user = new User();
        user.setId(1L);

        testEntry = new MoodEntry();
        testEntry.setId(1L);
        testEntry.setUser(user);
        testEntry.setDate(LocalDate.now());
        testEntry.setMoodValue(7);
        testEntry.setNote("Feeling good");
    }

    @Test
    public void testGetUserMoodEntries_ReturnsOk() throws Exception {
        when(moodEntryService.findByUserId(1L)).thenReturn(List.of(testEntry));

        mockMvc.perform(get("/api/mood-entries/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    public void testGetMoodEntryById_ReturnsOk() throws Exception {
        when(moodEntryService.findById(1L)).thenReturn(Optional.of(testEntry));

        mockMvc.perform(get("/api/mood-entries/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moodValue").value(7));
    }

    @Test
    public void testGetMoodEntryById_NotFound_Returns404() throws Exception {
        when(moodEntryService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/mood-entries/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateMoodEntry_Success_Returns201() throws Exception {
        when(moodEntryService.createMoodEntry(any(MoodEntry.class))).thenReturn(testEntry);

        mockMvc.perform(post("/api/mood-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEntry)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void testCreateMoodEntry_InvalidUser_Returns400() throws Exception {
        when(moodEntryService.createMoodEntry(any(MoodEntry.class)))
                .thenThrow(new IllegalArgumentException("User not found"));

        mockMvc.perform(post("/api/mood-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEntry)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateMoodEntry_Success_ReturnsOk() throws Exception {
        when(moodEntryService.updateMoodEntry(eq(1L), any(MoodEntry.class))).thenReturn(testEntry);

        mockMvc.perform(put("/api/mood-entries/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEntry)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moodValue").value(7));
    }

    @Test
    public void testDeleteMoodEntry_Success_ReturnsNoContent() throws Exception {
        doNothing().when(moodEntryService).deleteMoodEntry(1L);

        mockMvc.perform(delete("/api/mood-entries/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteMoodEntry_NotFound_Returns404() throws Exception {
        doThrow(new IllegalArgumentException("Not found")).when(moodEntryService).deleteMoodEntry(999L);

        mockMvc.perform(delete("/api/mood-entries/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetAverageMood_ReturnsOk() throws Exception {
        when(moodEntryService.calculateAverageMood(any(), any(), any())).thenReturn(7.5);

        mockMvc.perform(get("/api/mood-entries/user/1/average")
                        .param("startDate", LocalDate.now().minusDays(7).toString())
                        .param("endDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(7.5));
    }

    @Test
    public void testGetMoodEntriesByCategory_ReturnsOk() throws Exception {
        when(moodEntryService.findByUserIdAndMoodCategory(1L, MoodCategory.PLEASANT))
                .thenReturn(List.of(testEntry));

        mockMvc.perform(get("/api/mood-entries/user/1/category/PLEASANT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }
}
