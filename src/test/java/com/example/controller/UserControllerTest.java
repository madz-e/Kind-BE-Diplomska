package com.example.controller;

import com.example.model.User;
import com.example.service.UserService;
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

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private ObjectMapper objectMapper;
    private User testUser;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@test.com");
        testUser.setPassword("password");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setNotificationEnabled(false);
        testUser.setCompletedIntro(false);
    }

    @Test
    public void testGetAllUsers_ReturnsOk() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(testUser));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("test@test.com"));
    }

    @Test
    public void testFindById_ReturnsOk() throws Exception {
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    public void testFindById_NotFound_Returns404() throws Exception {
        when(userService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testFindByEmail_ReturnsOk() throws Exception {
        when(userService.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/users/email/test@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    public void testFindByEmail_NotFound_Returns404() throws Exception {
        when(userService.findByEmail("nobody@test.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/email/nobody@test.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateUser_Success_Returns201() throws Exception {
        when(userService.existsByEmail("test@test.com")).thenReturn(false);
        when(userService.createUser(any(User.class))).thenReturn(testUser);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    public void testCreateUser_DuplicateEmail_Returns409() throws Exception {
        when(userService.existsByEmail("test@test.com")).thenReturn(true);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isConflict());
    }

    @Test
    public void testUpdateUser_Success_ReturnsOk() throws Exception {
        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(testUser);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void testUpdateUser_NotFound_Returns404() throws Exception {
        when(userService.updateUser(eq(999L), any(User.class)))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(put("/api/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteUser_Success_ReturnsNoContent() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteUser_NotFound_Returns404() throws Exception {
        doThrow(new RuntimeException("User not found")).when(userService).deleteUser(999L);

        mockMvc.perform(delete("/api/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateNotificationSettings_ReturnsOk() throws Exception {
        testUser.setNotificationEnabled(true);
        when(userService.updateNotificationSettings(1L, true)).thenReturn(testUser);

        mockMvc.perform(patch("/api/users/1/notifications")
                        .param("enabled", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationEnabled").value(true));
    }

    @Test
    public void testCompleteIntro_ReturnsOk() throws Exception {
        testUser.setCompletedIntro(true);
        when(userService.completeIntro(1L)).thenReturn(testUser);

        mockMvc.perform(patch("/api/users/1/complete-intro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedIntro").value(true));
    }
}
