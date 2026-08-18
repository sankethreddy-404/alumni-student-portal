package com.alumniportal.repository;

import com.alumniportal.entity.MentorshipRequest;
import com.alumniportal.entity.MentorshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MentorshipRequestRepository extends JpaRepository<MentorshipRequest, Long> {
    List<MentorshipRequest> findByStudentIdOrderByCreatedAtDesc(Long studentId);
    List<MentorshipRequest> findByAlumniIdOrderByCreatedAtDesc(Long alumniId);
    long countByAlumniIdAndStatus(Long alumniId, MentorshipStatus status);
    long countByStatus(MentorshipStatus status);
}
