package com.alumniportal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardResponse {
    private long totalAlumni;
    private long activeAlumni;
    private long pendingAlumniApprovals;
    private long totalStudents;
    private long jobsPosted;
    private long pendingJobApprovals;
    private long mentorshipSessions;
    private long eventParticipation;
    private double averageProfileCompleteness;
}
