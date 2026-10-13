package com.example.dto;

import com.example.model.Habit;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Getter
public class HabitWithWeekDto {
        private Habit habit;
        private List<Map<String, Object>> weekGrid;
}
