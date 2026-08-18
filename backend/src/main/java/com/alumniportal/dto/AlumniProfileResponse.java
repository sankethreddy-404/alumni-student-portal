package com.alumniportal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlumniProfileResponse {
    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String company;
    private String domain;
    private String skills;
    private String location;
    private Integer graduationYear;
    private String currentRole;
    private Integer experience;
    private String achievements;
    private String bio;
    private String linkedinUrl;
    private String resumeFilePath;
    private boolean availableForMentorship;
    private LocalDateTime lastVerifiedAt;
    private Integer profileCompleteness;
    private List<String> missingFields;
}
