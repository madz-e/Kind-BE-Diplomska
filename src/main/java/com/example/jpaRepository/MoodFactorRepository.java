package com.example.jpaRepository;

import com.example.model.MoodFactor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MoodFactorRepository extends JpaRepository<MoodFactor, Long> {

}