package com.example.service.impl;

import com.example.jpaRepository.UserRepository;
import com.example.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    public void setup() {
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
    public void testCreateUser_Success() {
        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.createUser(testUser);

        assertNotNull(result);
        assertEquals("test@test.com", result.getEmail());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    public void testCreateUser_DuplicateEmail_ThrowsException() {
        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(testUser));
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testFindById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("test@test.com", result.get().getEmail());
    }

    @Test
    public void testFindById_NotFound_ReturnsEmpty() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<User> result = userService.findById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    public void testFindByEmail_Success() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findByEmail("test@test.com");

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    public void testUpdateUser_Success() {
        User updated = new User();
        updated.setFirstName("Jane");
        updated.setLastName("Smith");
        updated.setNotificationEnabled(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.updateUser(1L, updated);

        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertTrue(result.isNotificationEnabled());
    }

    @Test
    public void testUpdateUser_NotFound_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(999L, testUser));
    }

    @Test
    public void testDeleteUser_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteUser_NotFound_ThrowsException() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.deleteUser(999L));
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    public void testUpdateNotificationSettings_Enable() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.updateNotificationSettings(1L, true);

        assertTrue(result.isNotificationEnabled());
    }

    @Test
    public void testCompleteIntro_SetsTrue() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.completeIntro(1L);

        assertTrue(result.getCompletedIntro());
    }

    @Test
    public void testExistsByEmail_True() {
        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);
        assertTrue(userService.existsByEmail("test@test.com"));
    }

    @Test
    public void testExistsByEmail_False() {
        when(userRepository.existsByEmail("unknown@test.com")).thenReturn(false);
        assertFalse(userService.existsByEmail("unknown@test.com"));
    }
}
