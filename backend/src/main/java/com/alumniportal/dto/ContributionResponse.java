package com.alumniportal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContributionResponse {
    private Long alumniId;
    private String alumniName;
    private long jobsPosted;
    private long mentorshipSessions;
    private long eventsAttended;
    private long totalScore;
}
