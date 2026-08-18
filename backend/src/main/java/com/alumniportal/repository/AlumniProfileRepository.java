package com.alumniportal.repository;

import com.alumniportal.entity.AlumniProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AlumniProfileRepository extends JpaRepository<AlumniProfile, Long> {
    Optional<AlumniProfile> findByUserId(Long userId);
    List<AlumniProfile> findByAvailableForMentorshipTrue();

    @Query("SELECT a FROM AlumniProfile a WHERE a.lastVerifiedAt < :cutoff")
    List<AlumniProfile> findUnverifiedSince(@Param("cutoff") LocalDateTime cutoff);

    @Query("SELECT a FROM AlumniProfile a WHERE a.profileCompleteness < :threshold")
    List<AlumniProfile> findIncompleteProfiles(@Param("threshold") int threshold);

    @Query("SELECT AVG(a.profileCompleteness) FROM AlumniProfile a")
    Double averageCompleteness();
}
