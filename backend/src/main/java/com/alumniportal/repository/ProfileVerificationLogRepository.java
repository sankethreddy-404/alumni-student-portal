package com.alumniportal.repository;

import com.alumniportal.entity.ProfileVerificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProfileVerificationLogRepository extends JpaRepository<ProfileVerificationLog, Long> {
    List<ProfileVerificationLog> findByAlumniIdOrderByCreatedAtDesc(Long alumniId);
}
