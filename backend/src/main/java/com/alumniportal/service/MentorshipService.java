package com.alumniportal.service;

import com.alumniportal.dto.MentorshipRequestDto;
import com.alumniportal.dto.MentorshipResponse;
import com.alumniportal.entity.*;
import com.alumniportal.exception.ApiException;
import com.alumniportal.repository.AlumniProfileRepository;
import com.alumniportal.repository.MentorshipRequestRepository;
import com.alumniportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MentorshipService {

    private final MentorshipRequestRepository mentorshipRequestRepository;
    private final UserRepository userRepository;
    private final AlumniProfileRepository alumniProfileRepository;
    private final ContributionService contributionService;

    @Transactional
    public MentorshipResponse sendRequest(Long studentId, MentorshipRequestDto dto) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ApiException("Student not found", HttpStatus.NOT_FOUND));
        User alumni = userRepository.findById(dto.getAlumniId())
                .orElseThrow(() -> new ApiException("Alumni not found", HttpStatus.NOT_FOUND));

        AlumniProfile profile = alumniProfileRepository.findByUserId(alumni.getId())
                .orElseThrow(() -> new ApiException("Alumni profile not found", HttpStatus.NOT_FOUND));

        if (!profile.isAvailableForMentorship()) {
            throw new ApiException("This alumnus is not currently available for mentorship", HttpStatus.BAD_REQUEST);
        }

        MentorshipRequest request = MentorshipRequest.builder()
                .student(student)
                .alumni(alumni)
                .message(dto.getMessage())
                .status(MentorshipStatus.PENDING)
                .build();

        request = mentorshipRequestRepository.save(request);
        return toResponse(request);
    }

    @Transactional
    public MentorshipResponse updateStatus(Long requestId, MentorshipStatus status) {
        MentorshipRequest request = getEntity(requestId);
        request.setStatus(status);
        mentorshipRequestRepository.save(request);

        if (status == MentorshipStatus.COMPLETED) {
            contributionService.recordContribution(request.getAlumni().getId(),
                    ContributionType.MENTORSHIP_SESSION, request.getId());
        }
        return toResponse(request);
    }

    @Transactional
    public MentorshipResponse schedule(Long requestId, LocalDateTime scheduledAt) {
        MentorshipRequest request = getEntity(requestId);
        if (request.getStatus() != MentorshipStatus.ACCEPTED && request.getStatus() != MentorshipStatus.SCHEDULED) {
            throw new ApiException("Only accepted requests can be scheduled", HttpStatus.BAD_REQUEST);
        }
        request.setScheduledAt(scheduledAt);
        request.setStatus(MentorshipStatus.SCHEDULED);
        mentorshipRequestRepository.save(request);
        return toResponse(request);
    }

    public List<MentorshipResponse> getForStudent(Long studentId) {
        return mentorshipRequestRepository.findByStudentIdOrderByCreatedAtDesc(studentId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<MentorshipResponse> getForAlumni(Long alumniId) {
        return mentorshipRequestRepository.findByAlumniIdOrderByCreatedAtDesc(alumniId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    /** Confirms whether chat between these two users should be unlocked (i.e. an accepted/scheduled/completed mentorship exists). */
    public boolean isChatUnlocked(Long userA, Long userB) {
        List<MentorshipRequest> asStudent = mentorshipRequestRepository.findByStudentIdOrderByCreatedAtDesc(userA);
        List<MentorshipRequest> asAlumni = mentorshipRequestRepository.findByAlumniIdOrderByCreatedAtDesc(userA);

        boolean unlocked = asStudent.stream().anyMatch(r -> r.getAlumni().getId().equals(userB) && isUnlockedStatus(r.getStatus()));
        if (!unlocked) {
            unlocked = asAlumni.stream().anyMatch(r -> r.getStudent().getId().equals(userB) && isUnlockedStatus(r.getStatus()));
        }
        return unlocked;
    }

    private boolean isUnlockedStatus(MentorshipStatus status) {
        return status == MentorshipStatus.ACCEPTED || status == MentorshipStatus.SCHEDULED || status == MentorshipStatus.COMPLETED;
    }

    MentorshipRequest getEntity(Long id) {
        return mentorshipRequestRepository.findById(id)
                .orElseThrow(() -> new ApiException("Mentorship request not found", HttpStatus.NOT_FOUND));
    }

    public long countByStatus(MentorshipStatus status) {
        return mentorshipRequestRepository.countByStatus(status);
    }

    private MentorshipResponse toResponse(MentorshipRequest r) {
        return MentorshipResponse.builder()
                .id(r.getId())
                .studentId(r.getStudent().getId())
                .studentName(r.getStudent().getName())
                .alumniId(r.getAlumni().getId())
                .alumniName(r.getAlumni().getName())
                .message(r.getMessage())
                .status(r.getStatus())
                .scheduledAt(r.getScheduledAt())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
