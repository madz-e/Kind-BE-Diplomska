package com.example.service.impl;

import com.example.jpaRepository.HabitDailyLogRepository;   // CHANGED
import com.example.jpaRepository.HabitRepository;
import com.example.model.Habit;
import com.example.model.User;
import com.example.service.UserService;
import com.example.service.impl.HabitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HabitServiceTest {

    @Mock
    private HabitRepository habitRepository;

    @Mock
    private HabitDailyLogRepository habitDailyLogRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private HabitService habitService;

    private User testUser;
    private Habit testHabit;

    @BeforeEach
    public void setup() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@test.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPassword("password");

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
    public void testCreateHabit_Success() {
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        when(habitRepository.save(any(Habit.class))).thenReturn(testHabit);

        Habit newHabit = new Habit();
        newHabit.setName("Morning Meditation");
        newHabit.setDescription("5 min mindfulness");

        Habit result = habitService.createHabit(1L, newHabit);

        assertNotNull(result);
        assertEquals("Morning Meditation", result.getName());
        assertEquals(testUser, result.getUser());
        assertFalse(result.getIsSystemHabit());
        verify(habitRepository, times(1)).save(any(Habit.class));
    }

    @Test
    public void testCreateHabit_UserNotFound_ThrowsException() {
        when(userService.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> habitService.createHabit(999L, new Habit()));
        verify(habitRepository, never()).save(any());
    }

    @Test
    public void testCreateHabit_SetsCreationDateToToday() {
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        when(habitRepository.save(any(Habit.class))).thenAnswer(i -> i.getArgument(0));

        Habit newHabit = new Habit();
        newHabit.setName("Exercise");

        Habit result = habitService.createHabit(1L, newHabit);

        assertEquals(LocalDate.now(), result.getCreationDate());
    }

    @Test
    public void testGetAllHabits_ReturnsUserHabits() {
        when(habitRepository.findByUserIdOrderByCreationDateAsc(1L)).thenReturn(List.of(testHabit));

        List<Habit> result = habitService.getAllHabits(1L);

        assertEquals(1, result.size());
        assertEquals("Morning Meditation", result.get(0).getName());
    }

    @Test
    public void testGetAllHabits_EmptyList() {
        when(habitRepository.findByUserIdOrderByCreationDateAsc(1L)).thenReturn(List.of());

        List<Habit> result = habitService.getAllHabits(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetHabitById_Success() {
        when(habitRepository.findById(1L)).thenReturn(Optional.of(testHabit));

        Habit result = habitService.getHabitById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Morning Meditation", result.getName());
    }

    @Test
    public void testGetHabitById_NotFound_ThrowsException() {
        when(habitRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> habitService.getHabitById(999L));
    }

    @Test
    public void testDeleteHabit_Success() {
        // CHANGED: delete now looks the habit up, clears its logs, then removes it
        //          (the old path called deleteById and 500'd on habits that had logs).
        when(habitRepository.findById(1L)).thenReturn(Optional.of(testHabit));

        habitService.deleteHabit(1L);

        verify(habitDailyLogRepository, times(1)).deleteByHabitId(1L);
        verify(habitRepository, times(1)).delete(testHabit);
    }

    @Test
    public void testGetHabitStatistics_ReturnsCorrectFields() {
        when(habitRepository.findById(1L)).thenReturn(Optional.of(testHabit));

        Map<String, Object> stats = habitService.getHabitStatistics(1L);

        assertNotNull(stats);
        assertEquals(1L, stats.get("id"));
        assertEquals("Morning Meditation", stats.get("name"));
        assertEquals(LocalDate.now(), stats.get("createdDate"));
        assertEquals(false, stats.get("isSystemHabit"));
        assertTrue(stats.containsKey("daysActive"));
    }

    @Test
    public void testGetHabitStatistics_DaysActiveIsZeroForNewHabit() {
        testHabit.setCreationDate(LocalDate.now());
        when(habitRepository.findById(1L)).thenReturn(Optional.of(testHabit));

        Map<String, Object> stats = habitService.getHabitStatistics(1L);

        assertEquals(0L, stats.get("daysActive"));
    }

    @Test
    public void testAddDefaultHabits_CreatesDefaultHabits() {
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        when(habitRepository.save(any(Habit.class))).thenAnswer(i -> i.getArgument(0));

        habitService.addDefaultHabits(1L);

        verify(habitRepository, times(4)).save(any(Habit.class));
    }

    @Test
    public void testAddDefaultHabits_UserNotFound_ThrowsException() {
        when(userService.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> habitService.addDefaultHabits(999L));
        verify(habitRepository, never()).save(any());
    }
}