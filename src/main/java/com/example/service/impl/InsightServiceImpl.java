package com.example.service.impl;

import com.example.dto.InsightResponse;
import com.example.dto.RuleBasedInsight;
import com.example.dto.UserInsightContext;
import com.example.model.User;
import com.example.service.InsightService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InsightServiceImpl implements InsightService {

    private final InsightContextService insightContextService;
    private final RuleBasedInsightServiceImpl ruleBasedInsightService;
    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper;
    private final OllamaClient ollamaClient;

    @Override
    public InsightResponse generateInsight(User user) {
        UserInsightContext ctx = insightContextService.buildInsightContext(user);
        RuleBasedInsight rule = ruleBasedInsightService.evaluate(ctx);

        if (rule.requiresCrisisResources()) {
            return new InsightResponse(
                    "We noticed something in what you shared that concerns us deeply.",
                    "Please reach out to a crisis helpline or a trusted mental health professional right away. You are not alone — support is available.",
                    "RULE_BASED_FALLBACK",
                    rule,
                    null
            );
        }

        try {
//            String json = openAiClient.getRecommendation(ctx, rule);
            String json = ollamaClient.getRecommendation(ctx, rule);
            JsonNode node = objectMapper.readTree(json);
            String feelingSummary = node.get("feeling_summary").asText();
            String recommendation = node.get("recommendation").asText();
            String reason = node.has("reason") ? node.get("reason").asText(null) : null;
            return new InsightResponse(feelingSummary, recommendation, "LLM", rule, reason);
        } catch (Exception e) {
            return new InsightResponse(
                    fallbackSummary(rule.severity()),
                    fallbackRecommendation(rule.severity()),
                    "RULE_BASED_FALLBACK",
                    rule,
                    null
            );
        }
    }

    private String fallbackSummary(String severity) {
        return switch (severity) {
            case "ELEVATED_CONCERN" -> "It looks like you've been going through a really tough stretch lately.";
            case "MODERATE" -> "You've had a few low days recently, and that's completely okay.";
            default -> "You're doing well overall — keep nurturing the habits that make you feel good.";
        };
    }

    private String fallbackRecommendation(String severity) {
        return switch (severity) {
            case "ELEVATED_CONCERN" -> "Consider speaking with a counselor or therapist — reaching out for support is a sign of strength.";
            case "MODERATE" -> "Try a short walk or a few minutes of mindful breathing to give yourself a gentle reset.";
            default -> "Take a moment today to do one small thing that brings you joy.";
        };
    }
}
