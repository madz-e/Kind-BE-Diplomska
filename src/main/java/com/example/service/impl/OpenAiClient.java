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
public class OpenAiClient {

    private static final String CHAT_COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions";

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String model;

    public OpenAiClient(
            @Value("${openai.api.key:}") String apiKey,
            @Value("${openai.model:gpt-4o-mini}") String model
    ) {
        this.restTemplate = new RestTemplate();
        this.apiKey = apiKey;
        this.model = model;
    }

    @SuppressWarnings("unchecked")
    public String getRecommendation(UserInsightContext ctx, RuleBasedInsight rule) {
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", buildSystemPrompt(rule)),
                        Map.of("role", "user", "content", buildUserPrompt(ctx, rule))
                ),
                "response_format", Map.of("type", "json_object")
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                CHAT_COMPLETIONS_URL,
                new HttpEntity<>(requestBody, headers),
                Map.class
        );

        List<Map<String, Object>> choices =
                (List<Map<String, Object>>) response.getBody().get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return (String) message.get("content");
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
