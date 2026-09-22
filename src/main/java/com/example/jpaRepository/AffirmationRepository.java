package com.example.jpaRepository;

import com.example.model.Affirmation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AffirmationRepository extends JpaRepository<Affirmation, Long> {

    // Get random affirmation (for daily affirmation feature)
    @Query(value = "SELECT * FROM affirmations ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<Affirmation> findRandom();
}