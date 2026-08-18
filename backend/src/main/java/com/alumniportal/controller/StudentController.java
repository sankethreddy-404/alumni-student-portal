package com.alumniportal.controller;

import com.alumniportal.dto.*;
import com.alumniportal.security.CustomUserDetails;
import com.alumniportal.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentProfileService studentProfileService;
    private final ResumeParsingService resumeParsingService;
    private final FileStorageService fileStorageService;
    private final JobService jobService;
    private final JobApplicationService jobApplicationService;
    private final MentorshipService mentorshipService;
    private final EventService eventService;
    private final MaterialService materialService;

    @GetMapping("/profile")
    public ResponseEntity<StudentProfileResponse> getProfile(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(studentProfileService.getMyProfile(principal.getId()));
    }

    @PutMapping("/profile")
    public ResponseEntity<StudentProfileResponse> updateProfile(@AuthenticationPrincipal CustomUserDetails principal,
                                                                 @RequestBody StudentProfileRequest request) {
        return ResponseEntity.ok(studentProfileService.updateProfile(principal.getId(), request));
    }

    @PostMapping(value = "/profile/resume", consumes = "multipart/form-data")
    public ResponseEntity<StudentProfileResponse> uploadResume(@AuthenticationPrincipal CustomUserDetails principal,
                                                                @RequestParam("file") MultipartFile file) {
        String path = fileStorageService.store(file, "resumes");
        studentProfileService.setResumePath(principal.getId(), path);

        String text = resumeParsingService.extractText(file);
        String skillsCsv = resumeParsingService.joinSkills(resumeParsingService.extractSkills(text));
        studentProfileService.applyAutoFill(principal.getId(), skillsCsv);

        return ResponseEntity.ok(studentProfileService.getMyProfile(principal.getId()));
    }

    // ---- Job portal ----
    @GetMapping("/jobs")
    public ResponseEntity<List<JobResponse>> listJobs() {
        return ResponseEntity.ok(jobService.listApproved());
    }

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<JobResponse> getJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(jobService.getById(jobId));
    }

    @PostMapping(value = "/jobs/{jobId}/apply", consumes = "multipart/form-data")
    public ResponseEntity<JobApplicationResponse> apply(@AuthenticationPrincipal CustomUserDetails principal,
                                                         @PathVariable Long jobId,
                                                         @RequestParam("resume") MultipartFile resume) {
        return ResponseEntity.ok(jobApplicationService.apply(principal.getId(), jobId, resume));
    }

    @GetMapping("/applications")
    public ResponseEntity<List<JobApplicationResponse>> myApplications(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(jobApplicationService.getMyApplications(principal.getId()));
    }

    // ---- Mentorship (as the student side) ----
    @PostMapping("/mentorship/request")
    public ResponseEntity<MentorshipResponse> requestMentorship(@AuthenticationPrincipal CustomUserDetails principal,
                                                                 @RequestBody MentorshipRequestDto dto) {
        return ResponseEntity.ok(mentorshipService.sendRequest(principal.getId(), dto));
    }

    @GetMapping("/mentorship/requests")
    public ResponseEntity<List<MentorshipResponse>> myRequests(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(mentorshipService.getForStudent(principal.getId()));
    }

    // ---- Events & materials ----
    @GetMapping("/events")
    public ResponseEntity<List<EventResponse>> listEvents(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(eventService.listAll(principal.getId()));
    }

    @PostMapping("/events/{eventId}/register")
    public ResponseEntity<MessageResponse> registerEvent(@AuthenticationPrincipal CustomUserDetails principal,
                                                          @PathVariable Long eventId) {
        eventService.register(eventId, principal.getId());
        return ResponseEntity.ok(new MessageResponse("Registered for event successfully"));
    }

    @GetMapping("/materials")
    public ResponseEntity<List<MaterialResponse>> listMaterials() {
        return ResponseEntity.ok(materialService.listAll());
    }
}
