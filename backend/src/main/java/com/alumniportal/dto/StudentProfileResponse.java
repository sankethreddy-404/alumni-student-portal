package com.alumniportal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfileResponse {
    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String branch;
    private Integer graduationYear;
    private String skills;
    private String bio;
    private String resumeFilePath;
}
