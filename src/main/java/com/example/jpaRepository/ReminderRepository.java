package com.example.jpaRepository;

import com.example.model.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    // Get all reminders for a user
    List<Reminder> findByUserIdOrderByTimeOfDayAsc(Long userId);

    // Get enabled reminders only
    List<Reminder> findByUserIdAndEnabled(Long userId, Boolean enabled);
}