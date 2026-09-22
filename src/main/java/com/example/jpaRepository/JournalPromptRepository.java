package com.example.jpaRepository;

import com.example.model.JournalPrompt;
import com.example.model.enumerations.JournalPromptType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JournalPromptRepository extends JpaRepository<JournalPrompt, Long> {

    // Get prompts by type (GENERAL or ANT_EXERCISE)
    List<JournalPrompt> findByType(JournalPromptType type);
}