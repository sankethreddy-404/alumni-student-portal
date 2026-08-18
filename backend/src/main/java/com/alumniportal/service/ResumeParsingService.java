package com.alumniportal.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class ResumeParsingService {

    private final Tika tika = new Tika();

    // A reasonably broad, extensible dictionary of common professional/tech skills.
    // Real-world systems would use an NLP/NER model; this keyword-matching approach
    // is transparent, dependency-light, and fully deterministic.
    private static final List<String> SKILL_DICTIONARY = List.of(
            "java", "python", "javascript", "typescript", "react", "reactjs", "angular", "vue",
            "node.js", "nodejs", "express", "spring boot", "spring", "hibernate", "django", "flask",
            "fastapi", "mysql", "postgresql", "mongodb", "redis", "sql", "nosql", "aws", "azure",
            "gcp", "docker", "kubernetes", "jenkins", "git", "github", "ci/cd", "rest api",
            "graphql", "microservices", "html", "css", "sass", "tailwind", "bootstrap", "c++",
            "c#", ".net", "golang", "go", "rust", "kotlin", "swift", "flutter", "android", "ios",
            "machine learning", "deep learning", "data science", "pandas", "numpy", "tensorflow",
            "pytorch", "scikit-learn", "nlp", "computer vision", "excel", "power bi", "tableau",
            "figma", "photoshop", "ui/ux", "agile", "scrum", "jira", "linux", "bash", "shell scripting",
            "kafka", "rabbitmq", "elasticsearch", "spark", "hadoop", "terraform", "ansible",
            "cybersecurity", "networking", "project management", "communication", "leadership"
    );

    public String extractText(MultipartFile file) {
        try {
            return tika.parseToString(file.getInputStream());
        } catch (IOException | org.apache.tika.exception.TikaException e) {
            log.warn("Failed to parse resume with Tika: {}", e.getMessage());
            return "";
        }
    }

    public List<String> extractSkills(String text) {
        if (text == null || text.isBlank()) return List.of();
        String lower = text.toLowerCase();
        List<String> found = new ArrayList<>();
        for (String skill : SKILL_DICTIONARY) {
            if (lower.contains(skill.toLowerCase())) {
                found.add(skill);
            }
        }
        return found;
    }

    public String extractCompany(String text) {
        return extractAfterLabel(text, "company", "organization", "employer");
    }

    public String extractRole(String text) {
        return extractAfterLabel(text, "role", "designation", "position", "title", "job title");
    }

    public Integer extractExperienceYears(String text) {
        if (text == null || text.isBlank()) return null;
        // Looks for patterns like "5 years of experience" or "3+ years"
        Pattern pattern = Pattern.compile("(\\d{1,2})\\s*\\+?\\s*(?:years|yrs)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private String extractAfterLabel(String text, String... labels) {
        if (text == null || text.isBlank()) return null;
        for (String label : labels) {
            Pattern pattern = Pattern.compile(label + "\\s*[:\\-]\\s*(.+)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String value = matcher.group(1).trim();
                int newlineIdx = value.indexOf('\n');
                if (newlineIdx > 0) value = value.substring(0, newlineIdx).trim();
                if (!value.isEmpty() && value.length() < 100) return value;
            }
        }
        return null;
    }

    public String joinSkills(List<String> skills) {
        return String.join(", ", skills);
    }
}
