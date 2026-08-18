package com.alumniportal.service;

import com.alumniportal.dto.ContributionResponse;
import com.alumniportal.entity.ContributionHistory;
import com.alumniportal.entity.ContributionType;
import com.alumniportal.entity.Role;
import com.alumniportal.entity.User;
import com.alumniportal.repository.ContributionHistoryRepository;
import com.alumniportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContributionService {

    private final ContributionHistoryRepository contributionHistoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public void recordContribution(Long alumniId, ContributionType type, Long referenceId) {
        User alumni = userRepository.findById(alumniId).orElse(null);
        if (alumni == null) return;
        ContributionHistory history = ContributionHistory.builder()
                .alumni(alumni)
                .type(type)
                .referenceId(referenceId)
                .build();
        contributionHistoryRepository.save(history);
    }

    public ContributionResponse getDashboard(Long alumniId) {
        User alumni = userRepository.findById(alumniId).orElseThrow();
        long jobs = contributionHistoryRepository.countByAlumniIdAndType(alumniId, ContributionType.JOB_POSTED);
        long mentorship = contributionHistoryRepository.countByAlumniIdAndType(alumniId, ContributionType.MENTORSHIP_SESSION);
        long events = contributionHistoryRepository.countByAlumniIdAndType(alumniId, ContributionType.EVENT_ATTENDED);
        return build(alumni, jobs, mentorship, events);
    }

    public List<ContributionResponse> getTopContributors(int limit) {
        List<User> alumni = userRepository.findByRole(Role.ALUMNI);
        return alumni.stream()
                .map(a -> getDashboard(a.getId()))
                .sorted(Comparator.comparingLong(ContributionResponse::getTotalScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    private ContributionResponse build(User alumni, long jobs, long mentorship, long events) {
        long total = jobs * 3 + mentorship * 2 + events; // weighted contribution score
        return ContributionResponse.builder()
                .alumniId(alumni.getId())
                .alumniName(alumni.getName())
                .jobsPosted(jobs)
                .mentorshipSessions(mentorship)
                .eventsAttended(events)
                .totalScore(total)
                .build();
    }
}
