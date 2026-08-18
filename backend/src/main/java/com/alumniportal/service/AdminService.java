package com.alumniportal.service;

import com.alumniportal.dto.AdminDashboardResponse;
import com.alumniportal.dto.AlumniProfileResponse;
import com.alumniportal.entity.*;
import com.alumniportal.exception.ApiException;
import com.alumniportal.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final AlumniProfileRepository alumniProfileRepository;
    private final JobRepository jobRepository;
    private final MentorshipRequestRepository mentorshipRequestRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final ProfileScoreService profileScoreService;

    @Value("${app.profile.verification-interval-days}")
    private int verificationIntervalDays;

    public AdminDashboardResponse getDashboard() {
        long totalAlumni = userRepository.findByRole(Role.ALUMNI).size();
        long activeAlumni = userRepository.findByRole(Role.ALUMNI).stream().filter(User::isActive).count();
        long pendingAlumni = userRepository.findByRoleAndApproved(Role.ALUMNI, false).size();
        long totalStudents = userRepository.findByRole(Role.STUDENT).size();
        long jobsPosted = jobRepository.countByStatus(JobStatus.APPROVED);
        long pendingJobs = jobRepository.countByStatus(JobStatus.PENDING);
        long mentorshipSessions = mentorshipRequestRepository.countByStatus(MentorshipStatus.COMPLETED);
        long eventParticipation = eventRegistrationRepository.findAll().size();
        Double avgCompleteness = alumniProfileRepository.averageCompleteness();

        return AdminDashboardResponse.builder()
                .totalAlumni(totalAlumni)
                .activeAlumni(activeAlumni)
                .pendingAlumniApprovals(pendingAlumni)
                .totalStudents(totalStudents)
                .jobsPosted(jobsPosted)
                .pendingJobApprovals(pendingJobs)
                .mentorshipSessions(mentorshipSessions)
                .eventParticipation(eventParticipation)
                .averageProfileCompleteness(avgCompleteness != null ? avgCompleteness : 0.0)
                .build();
    }

    public List<User> getPendingAlumniRegistrations() {
        return userRepository.findByRoleAndApproved(Role.ALUMNI, false);
    }

    @Transactional
    public void approveAlumni(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        user.setApproved(true);
        userRepository.save(user);
    }

    @Transactional
    public void setUserActive(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        user.setActive(active);
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<AlumniProfileResponse> getIncompleteProfiles(int threshold) {
        return alumniProfileRepository.findIncompleteProfiles(threshold).stream()
                .map(p -> AlumniProfileResponse.builder()
                        .id(p.getId())
                        .userId(p.getUser().getId())
                        .name(p.getUser().getName())
                        .email(p.getUser().getEmail())
                        .company(p.getCompany())
                        .profileCompleteness(p.getProfileCompleteness())
                        .missingFields(profileScoreService.getMissingFields(p))
                        .build())
                .collect(Collectors.toList());
    }

    public double getAverageCompleteness() {
        Double avg = alumniProfileRepository.averageCompleteness();
        return avg != null ? avg : 0.0;
    }

    public List<AlumniProfileResponse> getUnverifiedAlumni() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(verificationIntervalDays);
        return alumniProfileRepository.findUnverifiedSince(cutoff).stream()
                .map(p -> AlumniProfileResponse.builder()
                        .id(p.getId())
                        .userId(p.getUser().getId())
                        .name(p.getUser().getName())
                        .email(p.getUser().getEmail())
                        .lastVerifiedAt(p.getLastVerifiedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
