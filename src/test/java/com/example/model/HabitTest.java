package com.example.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class HabitTest {

    @Test
    public void testHabitCreation() {
        User user = new User();
        user.setId(1L);

        Habit habit = new Habit();
        habit.setName("Morning Meditation");
        habit.setDescription("5 min mindfulness");
        habit.setColorHex("#4CAF50");
        habit.setIconIdentifier("meditation");
        habit.setCreationDate(LocalDate.now());
        habit.setIsSystemHabit(false);
        habit.setUser(user);

        assertEquals("Morning Meditation", habit.getName());
        assertEquals("5 min mindfulness", habit.getDescription());
        assertEquals("#4CAF50", habit.getColorHex());
        assertEquals("meditation", habit.getIconIdentifier());
        assertEquals(LocalDate.now(), habit.getCreationDate());
        assertFalse(habit.getIsSystemHabit());
        assertEquals(user, habit.getUser());
    }

    @Test
    public void testSystemHabitFlag() {
        Habit habit = new Habit();
        habit.setIsSystemHabit(true);
        assertTrue(habit.getIsSystemHabit());

        habit.setIsSystemHabit(false);
        assertFalse(habit.getIsSystemHabit());
    }

    @Test
    public void testHabitDailyLogCreation() {
        Habit habit = new Habit();
        habit.setId(1L);
        habit.setName("Exercise");

        HabitDailyLog log = new HabitDailyLog();
        log.setHabit(habit);
        log.setDate(LocalDate.now());
        log.setCompleted(true);

        assertEquals(habit, log.getHabit());
        assertEquals(LocalDate.now(), log.getDate());
        assertTrue(log.isCompleted());
    }

    @Test
    public void testHabitDailyLogToggle() {
        HabitDailyLog log = new HabitDailyLog();
        log.setCompleted(true);
        assertTrue(log.isCompleted());

        log.setCompleted(false);
        assertFalse(log.isCompleted());
    }
}