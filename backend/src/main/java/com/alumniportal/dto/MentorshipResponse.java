package com.alumniportal.dto;

import com.alumniportal.entity.MentorshipStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorshipResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long alumniId;
    private String alumniName;
    private String message;
    private MentorshipStatus status;
    private LocalDateTime scheduledAt;
    private LocalDateTime createdAt;
}
