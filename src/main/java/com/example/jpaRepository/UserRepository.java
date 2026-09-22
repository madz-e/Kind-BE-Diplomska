package com.example.jpaRepository;

import com.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // For login: Find user by email
    Optional<User> findByEmail(String email);

    // For registration: Check if email already taken
    boolean existsByEmail(String email);
}