package com.example.controller;

import com.example.model.Emotion;
import com.example.model.enumerations.MoodCategory;
import com.example.service.EmotionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmotionController.class)
public class EmotionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmotionService emotionService;

    private ObjectMapper objectMapper;
    private Emotion testEmotion;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();

        testEmotion = new Emotion();
        testEmotion.setId(1L);
        testEmotion.setName("Happy");
        testEmotion.setMoodCategory(MoodCategory.PLEASANT);
    }

    @Test
    public void testGetAllEmotions_ReturnsOk() throws Exception {
        when(emotionService.getAllEmotions()).thenReturn(List.of(testEmotion));

        mockMvc.perform(get("/api/emotions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Happy"));
    }

    @Test
    public void testGetEmotionById_ReturnsOk() throws Exception {
        when(emotionService.findById(1L)).thenReturn(Optional.of(testEmotion));

        mockMvc.perform(get("/api/emotions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moodCategory").value("PLEASANT"));
    }

    @Test
    public void testGetEmotionById_NotFound_Returns404() throws Exception {
        when(emotionService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/emotions/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetEmotionsByCategory_ReturnsOk() throws Exception {
        when(emotionService.findByMoodCategory(MoodCategory.PLEASANT)).thenReturn(List.of(testEmotion));

        mockMvc.perform(get("/api/emotions/category/PLEASANT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Happy"));
    }

    @Test
    public void testCreateEmotion_Returns201() throws Exception {
        when(emotionService.createEmotion(any(Emotion.class))).thenReturn(testEmotion);

        mockMvc.perform(post("/api/emotions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEmotion)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Happy"));
    }

    @Test
    public void testUpdateEmotion_ReturnsOk() throws Exception {
        when(emotionService.updateEmotion(eq(1L), any(Emotion.class))).thenReturn(testEmotion);

        mockMvc.perform(put("/api/emotions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEmotion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Happy"));
    }

    @Test
    public void testDeleteEmotion_ReturnsNoContent() throws Exception {
        doNothing().when(emotionService).deleteEmotion(1L);

        mockMvc.perform(delete("/api/emotions/1"))
                .andExpect(status().isNoContent());
    }
}
