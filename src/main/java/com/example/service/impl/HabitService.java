package com.example.service.impl;

import com.example.jpaRepository.HabitDailyLogRepository;   // CHANGED: to clear child logs on delete
import com.example.jpaRepository.HabitRepository;
import com.example.model.Habit;
import com.example.model.User;
import com.example.model.exceptions.HabitNotFoundException;
import com.example.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class HabitService {
    private final HabitRepository habitRepository;
    private final HabitDailyLogRepository habitDailyLogRepository;   // CHANGED
    private final UserService userService;

    // 1. Create custom habit (user creates their own)
    public Habit createHabit(Long userId, Habit habit) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        habit.setUser(user);
        habit.setCreationDate(LocalDate.now());
        habit.setIsSystemHabit(false); // User-created

        return habitRepository.save(habit);
    }

    // 2. Get ALL habits for user (premade + custom mixed together)
    public List<Habit> getAllHabits(Long userId) {
        return habitRepository.findByUserIdOrderByCreationDateAsc(userId);
    }

    // 1. Get single habit (simple)
    public Habit getHabitById(Long habitId) {
        return habitRepository.findById(habitId)
                .orElseThrow(() -> new HabitNotFoundException(habitId));
    }

    // 3. Delete ANY habit (user can delete premade or custom)
    public void deleteHabit(Long habitId) {
        // CHANGED: was `habitRepository.deleteById(habitId)`, which 500'd for any habit
        //          that already had completion logs (every default habit, once ticked off):
        //          the habit_daily_log FK has no ON DELETE CASCADE under ddl-auto=update,
        //          so Postgres rejected the parent delete.
        // Now: verify it exists (→ 404, not a silent no-op), wipe its logs, then delete it.
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new HabitNotFoundException(habitId));

        habitDailyLogRepository.deleteByHabitId(habitId);   // CHANGED: clear children first
        habitRepository.delete(habit);                      // CHANGED: now safe for default habits
    }


    // 5. Initialize default premade habits when user signs up
    public void addDefaultHabits(Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Add 4 default premade habits
        addHabit(user, "Morning Meditation", "5 min mindfulness", "#4CAF50", "meditation", true);
        addHabit(user, "Daily Exercise", "30 min activity", "#2196F3", "exercise", true);
        addHabit(user, "Drink Water", "Stay hydrated<3", "#03A9F4", "water", true);
        addHabit(user, "Evening Gratitude", "3 things I'm grateful for", "#FF9800", "gratitude", true);
    }

    // 3. Simple habit statistics
    public Map<String, Object> getHabitStatistics(Long habitId) {
        Habit habit = getHabitById(habitId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("id", habit.getId());
        stats.put("name", habit.getName());
        stats.put("createdDate", habit.getCreationDate());
        stats.put("daysActive",
                java.time.temporal.ChronoUnit.DAYS.between(
                        habit.getCreationDate(),
                        LocalDate.now()
                )
        );
        stats.put("isSystemHabit", habit.getIsSystemHabit());

        return stats;
    }

    // Helper method
    private void addHabit(User user, String name, String desc, String color, String icon, boolean isSystem) {
        Habit habit = new Habit();
        habit.setName(name);
        habit.setDescription(desc);
        habit.setColorHex(color);
        habit.setIconIdentifier(icon);
        habit.setCreationDate(LocalDate.now());
        habit.setIsSystemHabit(isSystem);
        habit.setUser(user);
        habitRepository.save(habit);
    }
}