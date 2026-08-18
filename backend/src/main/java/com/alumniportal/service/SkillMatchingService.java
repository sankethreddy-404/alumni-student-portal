package com.alumniportal.service;

import com.alumniportal.entity.MatchCategory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SkillMatchingService {

    // Thresholds for categorizing the computed match score
    private static final double HIGH_THRESHOLD = 75.0;
    private static final double MEDIUM_THRESHOLD = 40.0;

    /**
     * Match Score = (Matched Skills / Required Skills) x 100
     * Both input strings are comma-separated skill lists (case-insensitive, whitespace-trimmed).
     */
    public double computeMatchScore(String candidateSkillsCsv, String requiredSkillsCsv) {
        Set<String> candidateSkills = toNormalizedSet(candidateSkillsCsv);
        Set<String> requiredSkills = toNormalizedSet(requiredSkillsCsv);

        if (requiredSkills.isEmpty()) return 0.0;

        long matched = requiredSkills.stream().filter(candidateSkills::contains).count();
        double score = (matched * 100.0) / requiredSkills.size();
        return Math.round(score * 100.0) / 100.0; // round to 2 decimals
    }

    public MatchCategory categorize(double score) {
        if (score >= HIGH_THRESHOLD) return MatchCategory.HIGH;
        if (score >= MEDIUM_THRESHOLD) return MatchCategory.MEDIUM;
        return MatchCategory.LOW;
    }

    public Set<String> toNormalizedSet(String csv) {
        if (csv == null || csv.isBlank()) return new HashSet<>();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }
}
