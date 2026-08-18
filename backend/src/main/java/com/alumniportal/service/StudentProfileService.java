package com.alumniportal.service;

import com.alumniportal.dto.StudentProfileRequest;
import com.alumniportal.dto.StudentProfileResponse;
import com.alumniportal.entity.StudentProfile;
import com.alumniportal.entity.User;
import com.alumniportal.exception.ApiException;
import com.alumniportal.repository.StudentProfileRepository;
import com.alumniportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentProfileService {

    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;

    @Transactional
    public StudentProfileResponse getMyProfile(Long userId) {
        return toResponse(getOrCreate(userId));
    }

    @Transactional
    public StudentProfileResponse updateProfile(Long userId, StudentProfileRequest request) {
        StudentProfile profile = getOrCreate(userId);
        if (request.getBranch() != null) profile.setBranch(request.getBranch());
        if (request.getGraduationYear() != null) profile.setGraduationYear(request.getGraduationYear());
        if (request.getSkills() != null) profile.setSkills(request.getSkills());
        if (request.getBio() != null) profile.setBio(request.getBio());
        studentProfileRepository.save(profile);
        return toResponse(profile);
    }

    @Transactional
    public void setResumePath(Long userId, String path) {
        StudentProfile profile = getOrCreate(userId);
        profile.setResumeFilePath(path);
        studentProfileRepository.save(profile);
    }

    @Transactional
    public void applyAutoFill(Long userId, String skills) {
        StudentProfile profile = getOrCreate(userId);
        if (skills != null && !skills.isBlank() && (profile.getSkills() == null || profile.getSkills().isBlank())) {
            profile.setSkills(skills);
            studentProfileRepository.save(profile);
        }
    }

    private StudentProfile getOrCreate(Long userId) {
        return studentProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
                    StudentProfile profile = StudentProfile.builder().user(user).build();
                    return studentProfileRepository.save(profile);
                });
    }

    private StudentProfileResponse toResponse(StudentProfile profile) {
        User user = profile.getUser();
        return StudentProfileResponse.builder()
                .id(profile.getId())
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .branch(profile.getBranch())
                .graduationYear(profile.getGraduationYear())
                .skills(profile.getSkills())
                .bio(profile.getBio())
                .resumeFilePath(profile.getResumeFilePath())
                .build();
    }
}
