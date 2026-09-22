package com.example.service.impl;

import com.example.dto.RuleBasedInsight;
import com.example.dto.UserInsightContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class OllamaClient {

    private static final String CHAT_URL = "http://localhost:11434/api/chat";

    private final RestTemplate restTemplate;
    private final String model;

    public OllamaClient(
            @Value("${ollama.model:deepseek-r1}") String model
    ) {
        this.restTemplate = new RestTemplate();
        this.model = model;
    }

    @SuppressWarnings("unchecked")
    public String getRecommendation(UserInsightContext ctx, RuleBasedInsight rule) {
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "stream", false,
                "messages", List.of(
                        Map.of("role", "system", "content", buildSystemPrompt(rule)),
                        Map.of("role", "user", "content", buildUserPrompt(ctx, rule))
                ),
                "format", "json"
        );

        System.out.println("=== Calling Ollama at " + CHAT_URL + " with model " + model + " ===");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                CHAT_URL,
                new HttpEntity<>(requestBody, headers),
                Map.class
        );

        System.out.println("=== Ollama raw response: " + response.getBody() + " ===");

        Map<String, Object> message = (Map<String, Object>) response.getBody().get("message");
        String content = (String) message.get("content");

        System.out.println("=== Ollama message content: " + content + " ===");

        return stripThinkTags(content);
    }

    private String stripThinkTags(String content) {
        return content.replaceAll("(?s)<think>.*?</think>", "").trim();
    }

    private String buildSystemPrompt(RuleBasedInsight rule) {
        boolean serious = "ELEVATED_CONCERN".equals(rule.severity()) || "CRISIS_FLAG".equals(rule.severity());
        String crisisAddendum = serious
                ? " When the situation seems serious, gently encourage the user to reach out to a mental health professional alongside any suggestion."
                : "";
        return "You are a warm, supportive wellness companion — not a therapist, not a clinician. "
                + "You must never diagnose, prescribe, or claim to be a mental health professional."
                + crisisAddendum
                + " Reflect the user's recent emotional state with empathy and offer one concrete, gentle activity suggestion."
                + " You MUST respond ONLY with valid JSON in exactly this shape:"
                + " {\"feeling_summary\": \"<1-2 warm sentences reflecting the user's recent emotional state>\","
                + " \"recommendation\": \"<one concrete, gentle activity suggestion>\","
                + " \"reason\": \"<one brief sentence explaining why this activity helps — e.g. a psychological or physiological rationale>\"}"
                + " No extra keys, no text outside the JSON object.";
    }

    private String buildUserPrompt(UserInsightContext ctx, RuleBasedInsight rule) {
        return String.format(
                "User first name: %s%n"
                        + "Average mood last 7 days (1-10): %s%n"
                        + "Average mood last 14 days (1-10): %s%n"
                        + "Mood values last 7 days (chronological): %s%n"
                        + "Recent emotions: %s%n"
                        + "Recent contributing factors: %s%n"
                        + "Habit completion rates (last 7 days): %s%n"
                        + "Latest journal note: %s%n"
                        + "Mood trend: %s%n"
                        + "Wellbeing severity: %s",
                ctx.firstName(),
                ctx.averageMoodLast7Days() != null ? String.format("%.1f", ctx.averageMoodLast7Days()) : "no data",
                ctx.averageMoodLast14Days() != null ? String.format("%.1f", ctx.averageMoodLast14Days()) : "no data",
                ctx.moodValuesLast7Days().isEmpty() ? "no data" : ctx.moodValuesLast7Days(),
                ctx.recentEmotions().isEmpty() ? "none recorded" : String.join(", ", ctx.recentEmotions()),
                ctx.recentFactors().isEmpty() ? "none recorded" : String.join(", ", ctx.recentFactors()),
                ctx.habitCompletionRates().isEmpty() ? "no habits" : ctx.habitCompletionRates(),
                ctx.latestNote() != null ? ctx.latestNote() : "none",
                rule.moodTrend(),
                rule.severity()
        );
    }
}