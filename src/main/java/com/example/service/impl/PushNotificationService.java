package com.example.service.impl;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PushNotificationService {

    private static final String EXPO_URL = "https://exp.host/--/api/v2/push/send";
    private final RestTemplate restTemplate = new RestTemplate();

    public void send(List<String> tokens, String title, String body) {
        if (tokens.isEmpty()) {
            return;
        }

        List<Map<String, String>> messages = new ArrayList<>();
        for (String token : tokens) {
            messages.add(Map.of(
                    "to", token,
                    "title", title,
                    "body", body,
                    "sound", "default",
                    "channelId", "default"
            ));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<List<Map<String, String>>> request = new HttpEntity<>(messages, headers);

        try {
            String response = restTemplate.postForObject(EXPO_URL, request, String.class);
            System.out.println("Expo response: " + response);
        } catch (Exception e) {
            System.out.println("Push failed: " + e.getMessage());
        }
    }
}