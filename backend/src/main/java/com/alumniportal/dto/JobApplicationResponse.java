package com.alumniportal.dto;

import com.alumniportal.entity.ApplicationStatus;
import com.alumniportal.entity.MatchCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobApplicationResponse {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private String companyName;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private String resumeFilePath;
    private Double matchScore;
    private MatchCategory matchCategory;
    private ApplicationStatus status;
    private Integer rank;
    private LocalDateTime appliedAt;
}
