package com.example.jpaRepository;

import com.example.model.CalmingSound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CalmingSoundRepository extends JpaRepository<CalmingSound, Long> {
    List<CalmingSound> findAllByOrderByNameAsc();

    List<CalmingSound> findByNameContainingIgnoreCase(String searchTerm);

    Optional<CalmingSound> findByNameIgnoreCase(String trim);
}