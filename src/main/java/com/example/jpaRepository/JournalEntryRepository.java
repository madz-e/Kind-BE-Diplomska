package com.example.jpaRepository;

import com.example.model.JournalEntry;
import com.example.model.enumerations.EntryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {

    // Get all journal entries for a user
    List<JournalEntry> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Get journal entries in date range
    List<JournalEntry> findByUserIdAndCreatedAtBetween(Long userId,
                                                       LocalDateTime startDate,
                                                       LocalDateTime endDate);

    // Get journal entries by type (BLANK or PROMPT_BASED)
    List<JournalEntry> findByUserIdAndType(Long userId, EntryType type);

    // Count user's journal entries (for statistics)
    long countByUserId(Long userId);

    // Word frequency analysis: Get all journal content for processing
    @Query("SELECT j.content FROM JournalEntry j WHERE j.user.id = :userId " +
            "AND j.createdAt BETWEEN :startDate AND :endDate")
    List<String> findAllContentByUserIdAndDateRange(@Param("userId") Long userId,
                                                    @Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);
}