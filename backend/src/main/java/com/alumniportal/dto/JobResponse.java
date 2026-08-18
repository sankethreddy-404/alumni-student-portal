package com.alumniportal.dto;

import com.alumniportal.entity.JobStatus;
import com.alumniportal.entity.JobType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobResponse {
    private Long id;
    private Long postedById;
    private String postedByName;
    private String companyName;
    private String title;
    private String description;
    private String requiredSkills;
    private Integer experienceRequired;
    private String location;
    private String applyLink;
    private JobType type;
    private JobStatus status;
    private LocalDateTime createdAt;
    private Long applicantCount;
}
