package com.example.controller;

import com.example.dto.InsightResponse;
import com.example.model.User;
import com.example.service.InsightService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/insights")
@RequiredArgsConstructor
public class InsightController {

    private final InsightService insightService;

    @GetMapping("/today")
    public ResponseEntity<InsightResponse> getTodayInsight(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(insightService.generateInsight(user));
    }
}
