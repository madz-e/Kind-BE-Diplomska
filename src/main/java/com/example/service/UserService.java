package com.example.service;

import com.example.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> getAllUsers();

    User createUser(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    User updateUser(Long id, User user);

    void deleteUser(Long id);

    boolean existsByEmail(String email);

    User updateNotificationSettings(Long id, boolean notificationEnabled);

    User completeIntro(Long id);
}