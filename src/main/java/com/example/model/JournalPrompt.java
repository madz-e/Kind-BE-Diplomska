package com.example.model;

import com.example.model.enumerations.JournalPromptType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
// TODO check the logic of this
public class JournalPrompt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // This holds the main prompt question (for GENERAL) or the title (for ANT_EXERCISE)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String promptText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JournalPromptType type;
    @com.fasterxml.jackson.annotation.JsonIgnore
    @OneToMany(mappedBy = "journalPrompt")
    private Set<JournalEntry> journalEntries;

    // This holds the 7 structured questions only for the ANT_EXERCISE type.
    public String[] getQuestions() {
        if (type == JournalPromptType.ANT_EXERCISE) {
            return new String[]{
                    "What happened?",
                    "What emotions did I feel at the time? How intense were they?",
                    "What went through my mind?",
                    "Facts that support this thought?",
                    "Facts that provide evidence against this thought?",
                    "What can be an alternative thought?",
                    "How do I feel now?"
            };
        } else {
            return new String[]{promptText};
        }
    }
}
