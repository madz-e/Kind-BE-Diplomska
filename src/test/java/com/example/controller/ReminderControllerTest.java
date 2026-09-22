package com.example.controller;

import com.example.model.Reminder;
import com.example.model.User;
import com.example.model.enumerations.ReminderType;
import com.example.model.exceptions.ReminderNotFoundException;
import com.example.service.impl.ReminderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReminderController.class)
public class ReminderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReminderService reminderService;

    private ObjectMapper objectMapper;
    private User testUser;
    private Reminder testReminder;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        testUser = new User();
        testUser.setId(1L);

        testReminder = new Reminder();
        testReminder.setId(1L);
        testReminder.setUser(testUser);
        testReminder.setTimeOfDay(LocalTime.of(9, 0));
        testReminder.setDaysOfWeek("MON,TUE,WED");
        testReminder.setType(ReminderType.GENERAL);
        testReminder.setMessage("Morning check-in");
        testReminder.setEnabled(true);
    }

    @Test
    public void testGetAllReminders_ReturnsOk() throws Exception {
        when(reminderService.getAllReminders()).thenReturn(List.of(testReminder));

        mockMvc.perform(get("/api/reminders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].message").value("Morning check-in"));
    }

    @Test
    public void testGetReminderById_ReturnsOk() throws Exception {
        when(reminderService.getReminderById(1L)).thenReturn(testReminder);

        mockMvc.perform(get("/api/reminders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.type").value("GENERAL"));
    }

    @Test
    public void testGetReminderById_NotFound_Returns404() throws Exception {
        when(reminderService.getReminderById(999L)).thenThrow(new ReminderNotFoundException(999L));

        mockMvc.perform(get("/api/reminders/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void testGetUserReminders_ReturnsOk() throws Exception {
        when(reminderService.getUserReminders(1L)).thenReturn(List.of(testReminder));

        mockMvc.perform(get("/api/reminders/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    public void testCreateReminder_Success_Returns201() throws Exception {
        when(reminderService.createReminder(any(Reminder.class))).thenReturn(testReminder);

        mockMvc.perform(post("/api/reminders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testReminder)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.reminder.id").value(1));
    }

    @Test
    public void testCreateReminder_InvalidDays_Returns400() throws Exception {
        when(reminderService.createReminder(any(Reminder.class)))
                .thenThrow(new IllegalArgumentException("Days of week cannot be empty"));

        mockMvc.perform(post("/api/reminders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testReminder)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void testCreateGeneralReminder_Success_Returns201() throws Exception {
        when(reminderService.createGeneralReminder(any(), any(), any(), any())).thenReturn(testReminder);

        Map<String, Object> request = Map.of(
                "userId", 1,
                "timeOfDay", "09:00:00",
                "daysOfWeek", "MON,WED,FRI",
                "message", "Morning check-in"
        );

        mockMvc.perform(post("/api/reminders/general")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testCreateAffirmationReminder_Success_Returns201() throws Exception {
        testReminder.setType(ReminderType.AFFIRMATION);
        when(reminderService.createAffirmationReminder(any(), any(), any(), any())).thenReturn(testReminder);

        Map<String, Object> request = Map.of(
                "userId", 1,
                "affirmationId", 1,
                "timeOfDay", "08:00:00",
                "daysOfWeek", "MON,TUE"
        );

        mockMvc.perform(post("/api/reminders/affirmation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.reminder.type").value("AFFIRMATION"));
    }

    @Test
    public void testUpdateReminder_Success_ReturnsOk() throws Exception {
        when(reminderService.updateReminder(eq(1L), any(Reminder.class))).thenReturn(testReminder);

        mockMvc.perform(put("/api/reminders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testReminder)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void testToggleReminder_Disable_ReturnsOk() throws Exception {
        testReminder.setEnabled(false);
        when(reminderService.toggleReminder(1L, false)).thenReturn(testReminder);

        mockMvc.perform(patch("/api/reminders/1/toggle")
                        .param("enabled", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Reminder disabled"));
    }

    @Test
    public void testToggleReminder_Enable_ReturnsOk() throws Exception {
        when(reminderService.toggleReminder(1L, true)).thenReturn(testReminder);

        mockMvc.perform(patch("/api/reminders/1/toggle")
                        .param("enabled", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Reminder enabled"));
    }

    @Test
    public void testDeleteReminder_Success_ReturnsOk() throws Exception {
        doNothing().when(reminderService).deleteReminder(1L);

        mockMvc.perform(delete("/api/reminders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Reminder deleted successfully"));
    }

    @Test
    public void testDeleteReminder_NotFound_Returns404() throws Exception {
        doThrow(new RuntimeException("Reminder not found with id: 999"))
                .when(reminderService).deleteReminder(999L);

        mockMvc.perform(delete("/api/reminders/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void testGetReminderTypes_ReturnsAllTypes() throws Exception {
        mockMvc.perform(get("/api/reminders/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.types").isArray());
    }

    @Test
    public void testGetUserAffirmationReminders_ReturnsOk() throws Exception {
        testReminder.setType(ReminderType.AFFIRMATION);
        when(reminderService.getUserAffirmationReminders(1L)).thenReturn(List.of(testReminder));

        mockMvc.perform(get("/api/reminders/user/1/affirmations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("AFFIRMATION"));
    }

    @Test
    public void testGetUserGeneralReminders_ReturnsOk() throws Exception {
        when(reminderService.getUserGeneralReminders(1L)).thenReturn(List.of(testReminder));

        mockMvc.perform(get("/api/reminders/user/1/general"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("GENERAL"));
    }
}