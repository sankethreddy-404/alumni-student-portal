package com.alumniportal.dto;

import lombok.Data;

@Data
public class StudentProfileRequest {
    private String branch;
    private Integer graduationYear;
    private String skills;
    private String bio;
}
