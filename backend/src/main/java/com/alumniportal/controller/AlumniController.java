package com.alumniportal.controller;

import com.alumniportal.dto.*;
import com.alumniportal.entity.ResumeSource;
import com.alumniportal.security.CustomUserDetails;
import com.alumniportal.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/alumni")
@RequiredArgsConstructor
public class AlumniController {

    private final AlumniProfileService alumniProfileService;
    private final ResumeParsingService resumeParsingService;
    private final JobService jobService;
    private final MentorshipService mentorshipService;
    private final ContributionService contributionService;
    private final JobApplicationService jobApplicationService;

    @GetMapping("/profile")
    public ResponseEntity<AlumniProfileResponse> getProfile(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(alumniProfileService.getMyProfile(principal.getId()));
    }

    @PutMapping("/profile")
    public ResponseEntity<AlumniProfileResponse> updateProfile(@AuthenticationPrincipal CustomUserDetails principal,
                                                                @RequestBody AlumniProfileRequest request) {
        return ResponseEntity.ok(alumniProfileService.updateProfile(principal.getId(), request));
    }

    @PostMapping("/profile/verify")
    public ResponseEntity<MessageResponse> verifyProfile(@AuthenticationPrincipal CustomUserDetails principal) {
        alumniProfileService.markVerified(principal.getId());
        return ResponseEntity.ok(new MessageResponse("Profile verified successfully"));
    }

    // Auto-fill: upload resume, extract skills/company/role/experience and merge into profile
    @PostMapping(value = "/profile/autofill/resume", consumes = "multipart/form-data")
    public ResponseEntity<AlumniProfileResponse> autofillFromResume(@AuthenticationPrincipal CustomUserDetails principal,
                                                                     @RequestParam("file") MultipartFile file) {
        String text = resumeParsingService.extractText(file);
        List<String> skills = resumeParsingService.extractSkills(text);
        String skillsCsv = resumeParsingService.joinSkills(skills);
        String company = resumeParsingService.extractCompany(text);
        String role = resumeParsingService.extractRole(text);
        Integer experience = resumeParsingService.extractExperienceYears(text);

        alumniProfileService.applyAutoFill(principal.getId(), skillsCsv, company, role, experience, null);
        return ResponseEntity.ok(alumniProfileService.getMyProfile(principal.getId()));
    }

    // Store LinkedIn URL; profile details are populated separately.@PostMapping("/profile/autofill/linkedin")
    public ResponseEntity<AlumniProfileResponse> autofillFromLinkedin(@AuthenticationPrincipal CustomUserDetails principal,
                                                                       @RequestBody LinkedinUrlRequest request) {
        AlumniProfileRequest req = new AlumniProfileRequest();
        req.setLinkedinUrl(request.getUrl());
        return ResponseEntity.ok(alumniProfileService.updateProfile(principal.getId(), req));
    }

    @GetMapping("/directory")
    public ResponseEntity<List<AlumniProfileResponse>> directory(
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer graduationYear) {
        return ResponseEntity.ok(alumniProfileService.searchDirectory(company, domain, skill, location, graduationYear));
    }

    @GetMapping("/mentors")
    public ResponseEntity<List<AlumniProfileResponse>> availableMentors() {
        return ResponseEntity.ok(alumniProfileService.findAvailableForMentorship());
    }

    // ---- Jobs posted by this alumnus ----
    @PostMapping("/jobs")
    public ResponseEntity<JobResponse> postJob(@AuthenticationPrincipal CustomUserDetails principal,
                                                @RequestBody JobRequest request) {
        return ResponseEntity.ok(jobService.postJob(principal.getId(), request));
    }

    @GetMapping("/jobs")
    public ResponseEntity<List<JobResponse>> myJobs(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(jobService.listByAlumni(principal.getId()));
    }

    @GetMapping("/jobs/{jobId}/applicants")
    public ResponseEntity<List<JobApplicationResponse>> rankedApplicants(@PathVariable Long jobId) {
        return ResponseEntity.ok(jobApplicationService.getRankedApplicants(jobId));
    }

    @PostMapping("/jobs/{jobId}/shortlist-top/{topN}")
    public ResponseEntity<List<JobApplicationResponse>> autoShortlist(@PathVariable Long jobId, @PathVariable int topN) {
        return ResponseEntity.ok(jobApplicationService.autoShortlistTop(jobId, topN));
    }

    @PostMapping("/applications/{applicationId}/refer")
    public ResponseEntity<JobApplicationResponse> referCandidate(@PathVariable Long applicationId) {
        return ResponseEntity.ok(jobApplicationService.refer(applicationId));
    }

    @PostMapping("/applications/{applicationId}/reject")
    public ResponseEntity<JobApplicationResponse> rejectCandidate(@PathVariable Long applicationId) {
        return ResponseEntity.ok(jobApplicationService.reject(applicationId));
    }

    // ---- Mentorship (as the alumni/mentor side) ----
    @GetMapping("/mentorship/requests")
    public ResponseEntity<List<MentorshipResponse>> myMentorshipRequests(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(mentorshipService.getForAlumni(principal.getId()));
    }

    @PostMapping("/mentorship/requests/{requestId}/accept")
    public ResponseEntity<MentorshipResponse> accept(@PathVariable Long requestId) {
        return ResponseEntity.ok(mentorshipService.updateStatus(requestId, com.alumniportal.entity.MentorshipStatus.ACCEPTED));
    }

    @PostMapping("/mentorship/requests/{requestId}/reject")
    public ResponseEntity<MentorshipResponse> reject(@PathVariable Long requestId) {
        return ResponseEntity.ok(mentorshipService.updateStatus(requestId, com.alumniportal.entity.MentorshipStatus.REJECTED));
    }

    @PostMapping("/mentorship/requests/{requestId}/schedule")
    public ResponseEntity<MentorshipResponse> schedule(@PathVariable Long requestId, @RequestBody MentorshipScheduleDto dto) {
        return ResponseEntity.ok(mentorshipService.schedule(requestId, dto.getScheduledAt()));
    }

    @PostMapping("/mentorship/requests/{requestId}/complete")
    public ResponseEntity<MentorshipResponse> complete(@PathVariable Long requestId) {
        return ResponseEntity.ok(mentorshipService.updateStatus(requestId, com.alumniportal.entity.MentorshipStatus.COMPLETED));
    }

    // ---- Contribution dashboard ----
    @GetMapping("/contributions")
    public ResponseEntity<ContributionResponse> myContributions(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(contributionService.getDashboard(principal.getId()));
    }
}
