package com.alumniportal.controller;

import com.alumniportal.dto.*;
import com.alumniportal.entity.JobStatus;
import com.alumniportal.entity.User;
import com.alumniportal.service.AdminService;
import com.alumniportal.service.ContributionService;
import com.alumniportal.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final JobService jobService;
    private final ContributionService contributionService;

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> dashboard() {
        return ResponseEntity.ok(adminService.getDashboard());
    }

    // ---- Alumni approval ----
    @GetMapping("/alumni/pending")
    public ResponseEntity<List<UserSummary>> pendingAlumni() {
        return ResponseEntity.ok(adminService.getPendingAlumniRegistrations().stream()
                .map(UserSummary::from).collect(Collectors.toList()));
    }

    @PostMapping("/alumni/{userId}/approve")
    public ResponseEntity<MessageResponse> approveAlumni(@PathVariable Long userId) {
        adminService.approveAlumni(userId);
        return ResponseEntity.ok(new MessageResponse("Alumni approved successfully"));
    }

    // ---- User management ----
    @GetMapping("/users")
    public ResponseEntity<List<UserSummary>> allUsers() {
        return ResponseEntity.ok(adminService.getAllUsers().stream()
                .map(UserSummary::from).collect(Collectors.toList()));
    }

    @PostMapping("/users/{userId}/deactivate")
    public ResponseEntity<MessageResponse> deactivate(@PathVariable Long userId) {
        adminService.setUserActive(userId, false);
        return ResponseEntity.ok(new MessageResponse("User deactivated"));
    }

    @PostMapping("/users/{userId}/activate")
    public ResponseEntity<MessageResponse> activate(@PathVariable Long userId) {
        adminService.setUserActive(userId, true);
        return ResponseEntity.ok(new MessageResponse("User activated"));
    }

    // ---- Job approvals ----
    @GetMapping("/jobs/pending")
    public ResponseEntity<List<JobResponse>> pendingJobs() {
        return ResponseEntity.ok(jobService.listPending());
    }

    @PostMapping("/jobs/{jobId}/approve")
    public ResponseEntity<JobResponse> approveJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(jobService.setStatus(jobId, JobStatus.APPROVED));
    }

    @PostMapping("/jobs/{jobId}/reject")
    public ResponseEntity<JobResponse> rejectJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(jobService.setStatus(jobId, JobStatus.REJECTED));
    }

    // ---- Profile monitoring ----
    @GetMapping("/profiles/incomplete")
    public ResponseEntity<List<AlumniProfileResponse>> incompleteProfiles(
            @RequestParam(defaultValue = "70") int threshold) {
        return ResponseEntity.ok(adminService.getIncompleteProfiles(threshold));
    }

    @GetMapping("/profiles/unverified")
    public ResponseEntity<List<AlumniProfileResponse>> unverifiedProfiles() {
        return ResponseEntity.ok(adminService.getUnverifiedAlumni());
    }

    // ---- Top contributors ----
    @GetMapping("/contributors/top")
    public ResponseEntity<List<ContributionResponse>> topContributors(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(contributionService.getTopContributors(limit));
    }

    // Small inline DTO for user listings, defined as a static nested record for simplicity
    public record UserSummary(Long id, String name, String email, String role, boolean approved, boolean active) {
        public static UserSummary from(User u) {
            return new UserSummary(u.getId(), u.getName(), u.getEmail(), u.getRole().name(), u.isApproved(), u.isActive());
        }
    }
}
