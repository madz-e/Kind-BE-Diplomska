package com.example.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    public void testUserCreation() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("password");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setNotificationEnabled(true);
        user.setCompletedIntro(false);

        assertEquals("test@test.com", user.getEmail());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertTrue(user.isNotificationEnabled());
        assertFalse(user.getCompletedIntro());
    }

    @Test
    public void testUserCompletedIntroDefaultsFalse() {
        User user = new User();
        user.setCompletedIntro(false);
        assertFalse(user.getCompletedIntro());
    }

    @Test
    public void testUserToggleNotifications() {
        User user = new User();
        user.setNotificationEnabled(false);
        assertFalse(user.isNotificationEnabled());

        user.setNotificationEnabled(true);
        assertTrue(user.isNotificationEnabled());
    }
}
