package com.example.service;

import com.example.dto.InsightResponse;
import com.example.model.User;

public interface InsightService {
    InsightResponse generateInsight(User user);
}
