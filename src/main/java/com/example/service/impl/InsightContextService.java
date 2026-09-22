package com.example.service.impl;

import com.example.dto.UserInsightContext;
import com.example.jpaRepository.HabitDailyLogRepository;
import com.example.jpaRepository.HabitRepository;
import com.example.jpaRepository.MoodEntryRepository;
import com.example.model.Habit;
import com.example.model.MoodEntry;
import com.example.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InsightContextService {

    private final MoodEntryRepository moodEntryRepository;
    private final HabitDailyLogRepository habitDailyLogRepository;
    private final HabitRepository habitRepository;

    @Transactional(readOnly = true)
    public UserInsightContext buildInsightContext(User user) {
        Long userId = user.getId();
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(6);
        LocalDate fourteenDaysAgo = today.minusDays(13);

        List<MoodEntry> last7Days = moodEntryRepository
                .findByUserIdAndDateBetween(userId, sevenDaysAgo, today);
        last7Days.sort(Comparator.comparing(MoodEntry::getDate));

        Double avg7 = moodEntryRepository.calculateAverageMood(userId, sevenDaysAgo, today);
        Double avg14 = moodEntryRepository.calculateAverageMood(userId, fourteenDaysAgo, today);

        List<Integer> moodValues = last7Days.stream()
                .map(MoodEntry::getMoodValue)
                .collect(Collectors.toList());

        List<String> emotions = last7Days.stream()
                .flatMap(e -> e.getSelectedEmotions().stream())
                .map(em -> em.getName())
                .distinct()
                .collect(Collectors.toList());

        List<String> factors = last7Days.stream()
                .flatMap(e -> e.getSelectedFactors().stream())
                .map(f -> f.getName())
                .distinct()
                .collect(Collectors.toList());

        List<Habit> habits = habitRepository.findByUserIdOrderByCreationDateAsc(userId);
        Map<String, Double> completionRates = new LinkedHashMap<>();
        for (Habit habit : habits) {
            long completed = habitDailyLogRepository
                    .countCompletedInDateRange(habit.getId(), sevenDaysAgo, today);
            completionRates.put(habit.getName(), completed / 7.0);
        }

        String latestNote = last7Days.isEmpty() ? null
                : last7Days.get(last7Days.size() - 1).getNote();

        return new UserInsightContext(
                user.getFirstName(),
                avg7,
                avg14,
                moodValues,
                emotions,
                factors,
                completionRates,
                latestNote
        );
    }
}
