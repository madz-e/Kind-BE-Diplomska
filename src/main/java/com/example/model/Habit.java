package com.example.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Habit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    private String colorHex;

    private String iconIdentifier;

    @Column(nullable = false)
    private LocalDate creationDate;

    @Column(name = "is_system_habit", nullable = false)
    private Boolean isSystemHabit = false;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "moodEntries", "journalEntries", "habits", "reminders", "breathingSessions", "password", "authorities"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JsonIgnore
    @OneToMany(mappedBy = "habit", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<HabitDailyLog> dailyLogs;

}
