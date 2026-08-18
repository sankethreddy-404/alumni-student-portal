package com.alumniportal.repository;

import com.alumniportal.entity.ContributionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContributionHistoryRepository extends JpaRepository<ContributionHistory, Long> {
    List<ContributionHistory> findByAlumniId(Long alumniId);
    long countByAlumniIdAndType(Long alumniId, com.alumniportal.entity.ContributionType type);
}
