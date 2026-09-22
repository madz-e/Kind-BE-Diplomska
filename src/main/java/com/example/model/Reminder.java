package com.example.model;

import com.example.model.enumerations.ReminderType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reminder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalTime timeOfDay;

    @Column(nullable = false)
    private String daysOfWeek;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReminderType type;

    private String message;

    @Column(nullable = false)
    private boolean enabled;

    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // TODO how will be handled if we select a remindder that is not affiramtion
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "affirmation_id")
    private Affirmation linkedAffirmation;
}
