package com.example.service.impl;

import com.example.jpaRepository.ReminderRepository;
import com.example.model.Affirmation;
import com.example.model.Reminder;
import com.example.model.User;
import com.example.model.enumerations.ReminderType;
import com.example.service.UserService;
import com.example.service.impl.AffirmationService;
import com.example.service.impl.ReminderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReminderServiceTest {

    @Mock
    private ReminderRepository reminderRepository;

    @Mock
    private UserService userService;

    @Mock
    private AffirmationService affirmationService;

    @InjectMocks
    private ReminderService reminderService;

    private User testUser;
    private Reminder testReminder;
    private Affirmation testAffirmation;

    @BeforeEach
    public void setup() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@test.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPassword("password");

        testAffirmation = new Affirmation();
        testAffirmation.setId(1L);
        testAffirmation.setAffirmationText("I am enough.");

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
    public void testGetReminderById_Success() {
        when(reminderRepository.findById(1L)).thenReturn(Optional.of(testReminder));

        Reminder result = reminderService.getReminderById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Morning check-in", result.getMessage());
    }

    @Test
    public void testGetReminderById_NotFound_ThrowsException() {
        when(reminderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> reminderService.getReminderById(999L));
    }

    @Test
    public void testGetAllReminders() {
        when(reminderRepository.findAll()).thenReturn(List.of(testReminder));

        List<Reminder> result = reminderService.getAllReminders();

        assertEquals(1, result.size());
        verify(reminderRepository, times(1)).findAll();
    }

    @Test
    public void testGetUserReminders() {
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        when(reminderRepository.findByUserIdOrderByTimeOfDayAsc(1L)).thenReturn(List.of(testReminder));

        List<Reminder> result = reminderService.getUserReminders(1L);

        assertEquals(1, result.size());
        assertEquals(ReminderType.GENERAL, result.get(0).getType());
    }

    @Test
    public void testCreateGeneralReminder_Success() {
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        when(reminderRepository.save(any(Reminder.class))).thenAnswer(i -> i.getArgument(0));

        Reminder result = reminderService.createGeneralReminder(
                1L, LocalTime.of(9, 0), "MON,WED,FRI", "Morning check-in"
        );

        assertNotNull(result);
        assertEquals(ReminderType.GENERAL, result.getType());
        assertEquals("Morning check-in", result.getMessage());
        assertTrue(result.isEnabled());
        assertNull(result.getLinkedAffirmation());
    }

    @Test
    public void testCreateGeneralReminder_InvalidDays_ThrowsException() {
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class, () ->
                reminderService.createGeneralReminder(1L, LocalTime.of(9, 0), "", "Morning")
        );
    }

    @Test
    public void testCreateAffirmationReminder_Success() {
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        when(affirmationService.getAffirmationById(1L)).thenReturn(testAffirmation);
        when(reminderRepository.save(any(Reminder.class))).thenAnswer(i -> i.getArgument(0));

        Reminder result = reminderService.createAffirmationReminder(
                1L, 1L, LocalTime.of(8, 0), "MON,TUE"
        );

        assertNotNull(result);
        assertEquals(ReminderType.AFFIRMATION, result.getType());
        assertEquals("I am enough.", result.getMessage());
        assertEquals(testAffirmation, result.getLinkedAffirmation());
    }

    @Test
    public void testToggleReminder_Enable() {
        when(reminderRepository.findById(1L)).thenReturn(Optional.of(testReminder));
        when(reminderRepository.save(any(Reminder.class))).thenAnswer(i -> i.getArgument(0));

        Reminder result = reminderService.toggleReminder(1L, false);

        assertFalse(result.isEnabled());
    }

    @Test
    public void testUpdateReminder_Success() {
        Reminder updated = new Reminder();
        updated.setTimeOfDay(LocalTime.of(10, 0));
        updated.setDaysOfWeek("MON,FRI");
        updated.setEnabled(false);
        updated.setMessage("Updated message");

        when(reminderRepository.findById(1L)).thenReturn(Optional.of(testReminder));
        when(reminderRepository.save(any(Reminder.class))).thenAnswer(i -> i.getArgument(0));

        Reminder result = reminderService.updateReminder(1L, updated);

        assertEquals(LocalTime.of(10, 0), result.getTimeOfDay());
        assertEquals("MON,FRI", result.getDaysOfWeek());
        assertEquals("Updated message", result.getMessage());
        assertFalse(result.isEnabled());
    }

    @Test
    public void testDeleteReminder_Success() {
        when(reminderRepository.findById(1L)).thenReturn(Optional.of(testReminder));

        reminderService.deleteReminder(1L);

        verify(reminderRepository, times(1)).delete(testReminder);
    }

    @Test
    public void testDeleteReminder_NotFound_ThrowsException() {
        when(reminderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> reminderService.deleteReminder(999L));
        verify(reminderRepository, never()).delete(any());
    }

    @Test
    public void testGetUserAffirmationReminders() {
        Reminder affirmationReminder = new Reminder();
        affirmationReminder.setType(ReminderType.AFFIRMATION);
        affirmationReminder.setUser(testUser);
        affirmationReminder.setTimeOfDay(LocalTime.of(8, 0));
        affirmationReminder.setDaysOfWeek("MON");
        affirmationReminder.setEnabled(true);

        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        when(reminderRepository.findByUserIdOrderByTimeOfDayAsc(1L))
                .thenReturn(List.of(affirmationReminder, testReminder));

        List<Reminder> result = reminderService.getUserAffirmationReminders(1L);

        assertEquals(1, result.size());
        assertEquals(ReminderType.AFFIRMATION, result.get(0).getType());
    }

    @Test
    public void testGetUserGeneralReminders() {
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        when(reminderRepository.findByUserIdOrderByTimeOfDayAsc(1L)).thenReturn(List.of(testReminder));

        List<Reminder> result = reminderService.getUserGeneralReminders(1L);

        assertEquals(1, result.size());
        assertEquals(ReminderType.GENERAL, result.get(0).getType());
    }
}