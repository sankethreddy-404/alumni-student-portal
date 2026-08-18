package com.alumniportal.dto;

import com.alumniportal.entity.JobType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JobRequest {
    @NotBlank
    private String companyName;

    @NotBlank
    private String title;

    private String description;

    @NotBlank
    private String requiredSkills; // comma-separated

    private Integer experienceRequired;
    private String location;
    private String applyLink;
    private JobType type;
}
