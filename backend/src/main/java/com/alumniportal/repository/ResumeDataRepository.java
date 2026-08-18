package com.alumniportal.repository;

import com.alumniportal.entity.ResumeData;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResumeDataRepository extends JpaRepository<ResumeData, Long> {
    List<ResumeData> findByUserIdOrderByUploadedAtDesc(Long userId);
}
