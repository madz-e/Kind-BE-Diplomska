package com.example.controller;

import com.example.model.JournalEntry;
import com.example.model.JournalPrompt;
import com.example.model.User;
import com.example.model.enumerations.EntryType;
import com.example.model.enumerations.JournalPromptType;
import com.example.service.JournalEntryService;
import com.example.service.JournalPromptService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
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

@WebMvcTest(JournalEntryController.class)
public class JournalEntryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JournalEntryService journalEntryService;

    private ObjectMapper objectMapper;
    private JournalEntry testEntry;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        User user = new User();
        user.setId(1L);

        testEntry = new JournalEntry();
        testEntry.setId(1L);
        testEntry.setUser(user);
        testEntry.setTitle("My Day");
        testEntry.setContent("Today was great.");
        testEntry.setType(EntryType.BLANK);
        testEntry.setCreatedAt(LocalDate.now());
    }

    @Test
    public void testGetUserEntries_ReturnsOk() throws Exception {
        when(journalEntryService.findByUserId(1L)).thenReturn(List.of(testEntry));

        mockMvc.perform(get("/api/journal-entries/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    public void testGetEntryById_ReturnsOk() throws Exception {
        when(journalEntryService.findById(1L)).thenReturn(Optional.of(testEntry));

        mockMvc.perform(get("/api/journal-entries/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("My Day"));
    }

    @Test
    public void testGetEntryById_NotFound_Returns404() throws Exception {
        when(journalEntryService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/journal-entries/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateEntry_Success_Returns201() throws Exception {
        when(journalEntryService.createJournalEntry(any(JournalEntry.class))).thenReturn(testEntry);

        mockMvc.perform(post("/api/journal-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEntry)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void testCreateEntry_InvalidInput_Returns400() throws Exception {
        when(journalEntryService.createJournalEntry(any(JournalEntry.class)))
                .thenThrow(new IllegalArgumentException("Content cannot be empty"));

        mockMvc.perform(post("/api/journal-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEntry)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateEntry_Success_ReturnsOk() throws Exception {
        when(journalEntryService.updateJournalEntry(eq(1L), any(JournalEntry.class))).thenReturn(testEntry);

        mockMvc.perform(put("/api/journal-entries/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEntry)))
                .andExpect(status().isOk());
    }

    @Test
    public void testDeleteEntry_Success_ReturnsNoContent() throws Exception {
        doNothing().when(journalEntryService).deleteJournalEntry(1L);

        mockMvc.perform(delete("/api/journal-entries/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteEntry_NotFound_Returns404() throws Exception {
        doThrow(new IllegalArgumentException("Not found"))
                .when(journalEntryService).deleteJournalEntry(999L);

        mockMvc.perform(delete("/api/journal-entries/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetEntryCount_ReturnsOk() throws Exception {
        when(journalEntryService.countByUserId(1L)).thenReturn(5L);

        mockMvc.perform(get("/api/journal-entries/user/1/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5));
    }

    @Test
    public void testGetEntriesByType_ReturnsOk() throws Exception {
        when(journalEntryService.findByUserIdAndType(1L, EntryType.BLANK)).thenReturn(List.of(testEntry));

        mockMvc.perform(get("/api/journal-entries/user/1/type/BLANK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("BLANK"));
    }
}
