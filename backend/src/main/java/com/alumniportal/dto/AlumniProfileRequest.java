package com.alumniportal.dto;

import lombok.Data;

@Data
public class AlumniProfileRequest {
    private String company;
    private String domain;
    private String skills; // comma-separated
    private String location;
    private Integer graduationYear;
    private String currentRole;
    private Integer experience;
    private String achievements;
    private String bio;
    private String linkedinUrl;
    private Boolean availableForMentorship;
}
