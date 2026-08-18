package com.alumniportal.service;

import com.alumniportal.dto.AlumniProfileRequest;
import com.alumniportal.dto.AlumniProfileResponse;
import com.alumniportal.entity.AlumniProfile;
import com.alumniportal.entity.User;
import com.alumniportal.exception.ApiException;
import com.alumniportal.repository.AlumniProfileRepository;
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
public class AlumniProfileService {

    private final AlumniProfileRepository alumniProfileRepository;
    private final UserRepository userRepository;
    private final ProfileScoreService profileScoreService;

    @Transactional
    public AlumniProfileResponse getMyProfile(Long userId) {
        AlumniProfile profile = getOrCreate(userId);
        return toResponse(profile);
    }

    @Transactional
    public AlumniProfileResponse updateProfile(Long userId, AlumniProfileRequest request) {
        AlumniProfile profile = getOrCreate(userId);

        if (request.getCompany() != null) profile.setCompany(request.getCompany());
        if (request.getDomain() != null) profile.setDomain(request.getDomain());
        if (request.getSkills() != null) profile.setSkills(request.getSkills());
        if (request.getLocation() != null) profile.setLocation(request.getLocation());
        if (request.getGraduationYear() != null) profile.setGraduationYear(request.getGraduationYear());
        if (request.getCurrentRole() != null) profile.setCurrentRole(request.getCurrentRole());
        if (request.getExperience() != null) profile.setExperience(request.getExperience());
        if (request.getAchievements() != null) profile.setAchievements(request.getAchievements());
        if (request.getBio() != null) profile.setBio(request.getBio());
        if (request.getLinkedinUrl() != null) profile.setLinkedinUrl(request.getLinkedinUrl());
        if (request.getAvailableForMentorship() != null) profile.setAvailableForMentorship(request.getAvailableForMentorship());

        profile.setProfileCompleteness(profileScoreService.calculateCompleteness(profile));
        alumniProfileRepository.save(profile);

        return toResponse(profile);
    }

    @Transactional
    public void applyAutoFill(Long userId, String skills, String company, String role, Integer experience, String resumeFilePath) {
        AlumniProfile profile = getOrCreate(userId);

        if (skills != null && !skills.isBlank() && (profile.getSkills() == null || profile.getSkills().isBlank())) {
            profile.setSkills(skills);
        }
        if (company != null && !company.isBlank() && (profile.getCompany() == null || profile.getCompany().isBlank())) {
            profile.setCompany(company);
        }
        if (role != null && !role.isBlank() && (profile.getCurrentRole() == null || profile.getCurrentRole().isBlank())) {
            profile.setCurrentRole(role);
        }
        if (experience != null && profile.getExperience() == null) {
            profile.setExperience(experience);
        }
        if (resumeFilePath != null) {
            profile.setResumeFilePath(resumeFilePath);
        }

        profile.setProfileCompleteness(profileScoreService.calculateCompleteness(profile));
        alumniProfileRepository.save(profile);
    }

    @Transactional
    public void markVerified(Long userId) {
        AlumniProfile profile = getOrCreate(userId);
        profile.setLastVerifiedAt(LocalDateTime.now());
        alumniProfileRepository.save(profile);
    }

    public List<AlumniProfileResponse> searchDirectory(String company, String domain, String skill, String location, Integer graduationYear) {
        return alumniProfileRepository.findAll().stream()
                .filter(p -> company == null || (p.getCompany() != null && p.getCompany().toLowerCase().contains(company.toLowerCase())))
                .filter(p -> domain == null || (p.getDomain() != null && p.getDomain().toLowerCase().contains(domain.toLowerCase())))
                .filter(p -> skill == null || (p.getSkills() != null && p.getSkills().toLowerCase().contains(skill.toLowerCase())))
                .filter(p -> location == null || (p.getLocation() != null && p.getLocation().toLowerCase().contains(location.toLowerCase())))
                .filter(p -> graduationYear == null || graduationYear.equals(p.getGraduationYear()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<AlumniProfileResponse> findAvailableForMentorship() {
        return alumniProfileRepository.findByAvailableForMentorshipTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private AlumniProfile getOrCreate(Long userId) {
        return alumniProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
                    AlumniProfile profile = AlumniProfile.builder().user(user).build();
                    return alumniProfileRepository.save(profile);
                });
    }

    private AlumniProfileResponse toResponse(AlumniProfile profile) {
        User user = profile.getUser();
        return AlumniProfileResponse.builder()
                .id(profile.getId())
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .company(profile.getCompany())
                .domain(profile.getDomain())
                .skills(profile.getSkills())
                .location(profile.getLocation())
                .graduationYear(profile.getGraduationYear())
                .currentRole(profile.getCurrentRole())
                .experience(profile.getExperience())
                .achievements(profile.getAchievements())
                .bio(profile.getBio())
                .linkedinUrl(profile.getLinkedinUrl())
                .resumeFilePath(profile.getResumeFilePath())
                .availableForMentorship(profile.isAvailableForMentorship())
                .lastVerifiedAt(profile.getLastVerifiedAt())
                .profileCompleteness(profile.getProfileCompleteness())
                .missingFields(profileScoreService.getMissingFields(profile))
                .build();
    }
}
