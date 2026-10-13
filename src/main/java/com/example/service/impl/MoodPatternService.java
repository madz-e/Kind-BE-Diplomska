package com.example.service.impl;

import com.example.dto.DaySummary;
import com.example.dto.MoodPattern;
import com.example.jpaRepository.*;
import com.example.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.function.ToDoubleFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MoodPatternService {

    private static final int WINDOW_DAYS = 30;
    private static final int MIN_COUNT = 3;           // min days/entries in EACH group
    private static final double MIN_MOOD_DIFF = 0.8;  // on the 1-5 scale
    private static final double MIN_SHARE_DIFF = 0.3; // 30 percentage points
    private static final double MOOD_RANGE = 4.0;     // 5 - 1
    private static final double JOURNAL_WEIGHT = 0.8; // keyword mentions = weaker signal
    private static final int MAX_PATTERNS = 3;

    private final MoodEntryRepository moodEntryRepository;
    private final HabitRepository habitRepository;
    private final HabitDailyLogRepository habitDailyLogRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final MoodFactorRepository moodFactorRepository;
    private final EmotionRepository emotionRepository;

    @Transactional(readOnly = true)
    public List<MoodPattern> findPatterns(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(WINDOW_DAYS - 1);

        List<MoodEntry> entries = moodEntryRepository.findByUserIdAndDateBetween(userId, start, today);
        if (entries.isEmpty()) return List.of();

        Map<LocalDate, DaySummary> days = summarizeDays(entries);
        List<Habit> habits = habitRepository.findByUserIdOrderByCreationDateAsc(userId);
        Map<Long, Set<LocalDate>> doneDays = completedDaysByHabit(userId, start, today);

        // Only the detailed flow asks about factors (and requires at least one).
        // Quick entries were never asked, so they must not count as "factor absent".
        List<MoodEntry> detailed = entries.stream()
                .filter(e -> !e.getSelectedFactors().isEmpty())
                .toList();

        List<MoodPattern> all = new ArrayList<>();
        all.addAll(habitMoodPatterns(days, habits, doneDays));
        all.addAll(factorMoodPatterns(detailed));
        all.addAll(habitFactorPatterns(detailed, habits, doneDays));
        all.addAll(journalMoodPatterns(userId, start, today, entries, days));

        return all.stream()
                .sorted(Comparator.comparingDouble(MoodPattern::strength).reversed())
                .limit(MAX_PATTERNS)
                .toList();
    }

    // ── Preparation ────────────────────────────────────────────────────────────

    private Map<LocalDate, DaySummary> summarizeDays(List<MoodEntry> entries) {
        Map<LocalDate, List<Integer>> moodsByDay = entries.stream()
                .collect(Collectors.groupingBy(MoodEntry::getDate,
                        Collectors.mapping(MoodEntry::getMoodValue, Collectors.toList())));

        Map<LocalDate, DaySummary> days = new TreeMap<>();
        moodsByDay.forEach((date, moods) -> days.put(date, DaySummary.of(moods)));
        return days;
    }

    private Map<Long, Set<LocalDate>> completedDaysByHabit(Long userId, LocalDate start, LocalDate end) {
        return habitDailyLogRepository.findCompletedByUserInRange(userId, start, end).stream()
                .collect(Collectors.groupingBy(log -> log.getHabit().getId(),
                        Collectors.mapping(HabitDailyLog::getDate, Collectors.toSet())));
    }

    // ── A. Mood + habit (per day, all entries) ─────────────────────────────────

    private List<MoodPattern> habitMoodPatterns(Map<LocalDate, DaySummary> days,
                                                List<Habit> habits,
                                                Map<Long, Set<LocalDate>> doneDays) {
        List<MoodPattern> result = new ArrayList<>();
        for (Habit habit : habits) {
            Set<LocalDate> done = doneDays.getOrDefault(habit.getId(), Set.of());
            List<DaySummary> with = new ArrayList<>();
            List<DaySummary> without = new ArrayList<>();

            for (Map.Entry<LocalDate, DaySummary> day : days.entrySet()) {
                if (day.getKey().isBefore(habit.getCreationDate())) continue; // habit didn't exist yet
                (done.contains(day.getKey()) ? with : without).add(day.getValue());
            }
            if (with.size() < MIN_COUNT || without.size() < MIN_COUNT) continue;

            // try all three measures, keep only the strongest for this habit
            MoodPattern best = null;
            best = stronger(best, compareDays(habit.getName(), "Average mood", with, without, DaySummary::average));
            best = stronger(best, compareDays(habit.getName(), "Lowest mood of the day", with, without, DaySummary::lowest));
            best = stronger(best, compareDays(habit.getName(), "Mood swing within the day (highest minus lowest)", with, without, DaySummary::swing));
            if (best != null) result.add(best);
        }
        return result;
    }

    private MoodPattern compareDays(String habitName, String metric,
                                    List<DaySummary> with, List<DaySummary> without,
                                    ToDoubleFunction<DaySummary> pick) {
        double a = with.stream().mapToDouble(pick).average().orElse(0);
        double b = without.stream().mapToDouble(pick).average().orElse(0);
        if (Math.abs(a - b) < MIN_MOOD_DIFF) return null;

        String sentence = String.format(Locale.US,
                "%s on days '%s' was completed: %.1f (%d days) vs %.1f on days it wasn't (%d days).",
                metric, habitName, a, with.size(), b, without.size());
        return new MoodPattern("HABIT_MOOD", sentence, Math.abs(a - b) / MOOD_RANGE);
    }

    // ── B. Mood + factor (per entry, detailed only) ────────────────────────────

    private List<MoodPattern> factorMoodPatterns(List<MoodEntry> detailed) {
        List<MoodPattern> result = new ArrayList<>();
        for (String factor : factorNames(detailed)) {
            List<Double> with = new ArrayList<>();
            List<Double> without = new ArrayList<>();
            for (MoodEntry e : detailed) {
                (hasFactor(e, factor) ? with : without).add((double) e.getMoodValue());
            }
            if (with.size() < MIN_COUNT || without.size() < MIN_COUNT) continue;

            double a = average(with), b = average(without);
            if (Math.abs(a - b) < MIN_MOOD_DIFF) continue;

            String sentence = String.format(Locale.US,
                    "Mood in entries where '%s' was selected as a factor: %.1f (%d entries) vs %.1f in entries without it (%d entries).",
                    factor, a, with.size(), b, without.size());
            result.add(new MoodPattern("FACTOR_MOOD", sentence, Math.abs(a - b) / MOOD_RANGE));
        }
        return result;
    }

    // ── C. Factor + habit (detailed entries, joined with habit logs by date) ───

    private List<MoodPattern> habitFactorPatterns(List<MoodEntry> detailed,
                                                  List<Habit> habits,
                                                  Map<Long, Set<LocalDate>> doneDays) {
        List<MoodPattern> result = new ArrayList<>();
        for (Habit habit : habits) {
            Set<LocalDate> done = doneDays.getOrDefault(habit.getId(), Set.of());
            List<MoodEntry> eligible = detailed.stream()
                    .filter(e -> !e.getDate().isBefore(habit.getCreationDate()))
                    .toList();

            List<MoodEntry> withHabit = eligible.stream().filter(e -> done.contains(e.getDate())).toList();
            List<MoodEntry> withoutHabit = eligible.stream().filter(e -> !done.contains(e.getDate())).toList();
            if (withHabit.size() < MIN_COUNT || withoutHabit.size() < MIN_COUNT) continue;

            MoodPattern best = null;
            for (String factor : factorNames(eligible)) {
                double shareWith = share(withHabit, factor);
                double shareWithout = share(withoutHabit, factor);
                double diff = Math.abs(shareWith - shareWithout);
                if (diff < MIN_SHARE_DIFF) continue;

                String sentence = String.format(Locale.US,
                        "'%s' was selected as a factor in %d%% of entries on days '%s' was completed (%d entries) vs %d%% on days it wasn't (%d entries).",
                        factor, Math.round(shareWith * 100), habit.getName(), withHabit.size(),
                        Math.round(shareWithout * 100), withoutHabit.size());
                best = stronger(best, new MoodPattern("HABIT_FACTOR", sentence, diff));
            }
            if (best != null) result.add(best);
        }
        return result;
    }

    // ── D. Journal keywords + mood (per day, only days that have text) ─────────

    private List<MoodPattern> journalMoodPatterns(Long userId, LocalDate start, LocalDate end,
                                                  List<MoodEntry> entries,
                                                  Map<LocalDate, DaySummary> days) {
        // collect all written text per day: journal title + content + CBT data + mood notes
        Map<LocalDate, StringBuilder> textByDay = new HashMap<>();
        for (JournalEntry j : journalEntryRepository.findByUserIdAndCreatedAtBetweenOrderByCreatedAtAsc(userId, start, end)) {
            StringBuilder sb = textByDay.computeIfAbsent(j.getCreatedAt(), d -> new StringBuilder());
            appendIfPresent(sb, j.getTitle());
            appendIfPresent(sb, j.getContent());
            appendIfPresent(sb, j.getStructuredData());
        }
        for (MoodEntry e : entries) {
            if (e.getNote() != null && !e.getNote().isBlank()) {
                appendIfPresent(textByDay.computeIfAbsent(e.getDate(), d -> new StringBuilder()), e.getNote());
            }
        }

        // only days that have BOTH text and a mood; days without text are "unknown", not "not mentioned"
        Map<LocalDate, String> textDays = new HashMap<>();
        textByDay.forEach((date, sb) -> { if (days.containsKey(date)) textDays.put(date, sb.toString()); });
        if (textDays.size() < MIN_COUNT * 2) return List.of();

        List<MoodPattern> result = new ArrayList<>();
        for (String keyword : keywords()) {
            // whole word, allowing endings: "sleep" matches "sleep", "sleeping", "sleepy"
            Pattern p = Pattern.compile("\\b" + Pattern.quote(keyword) + "\\w*",
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

            List<Double> with = new ArrayList<>();
            List<Double> without = new ArrayList<>();
            textDays.forEach((date, text) ->
                    (p.matcher(text).find() ? with : without).add(days.get(date).average()));
            if (with.size() < MIN_COUNT || without.size() < MIN_COUNT) continue;

            double a = average(with), b = average(without);
            if (Math.abs(a - b) < MIN_MOOD_DIFF) continue;

            String sentence = String.format(Locale.US,
                    "Average mood on days the user's writing mentioned '%s': %.1f (%d days) vs %.1f on writing days that didn't (%d days).",
                    keyword, a, with.size(), b, without.size());
            result.add(new MoodPattern("JOURNAL_MOOD", sentence, Math.abs(a - b) / MOOD_RANGE * JOURNAL_WEIGHT));
        }
        return result;
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private Set<String> factorNames(List<MoodEntry> entries) {
        return entries.stream()
                .flatMap(e -> e.getSelectedFactors().stream())
                .map(MoodFactor::getName)
                .collect(Collectors.toSet());
    }

    private boolean hasFactor(MoodEntry e, String factor) {
        return e.getSelectedFactors().stream().anyMatch(f -> f.getName().equals(factor));
    }

    private double share(List<MoodEntry> entries, String factor) {
        return entries.stream().filter(e -> hasFactor(e, factor)).count() / (double) entries.size();
    }

    private List<String> keywords() {
        Set<String> words = new LinkedHashSet<>();
        moodFactorRepository.findAll().forEach(f -> words.add(f.getName().toLowerCase()));
        emotionRepository.findAll().forEach(e -> words.add(e.getName().toLowerCase()));
        return new ArrayList<>(words);
    }

    private void appendIfPresent(StringBuilder sb, String text) {
        if (text != null && !text.isBlank()) sb.append(' ').append(text);
    }

    private double average(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    private MoodPattern stronger(MoodPattern a, MoodPattern b) {
        if (a == null) return b;
        if (b == null) return a;
        return b.strength() > a.strength() ? b : a;
    }
}