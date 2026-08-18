package com.alumniportal.controller;

import com.alumniportal.dto.JobResponse;
import com.alumniportal.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Generic, role-agnostic job endpoints. Role-specific actions (posting, applying,
// approving) live under /api/alumni, /api/student and /api/admin respectively.
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @GetMapping
    public ResponseEntity<List<JobResponse>> listApproved() {
        return ResponseEntity.ok(jobService.listApproved());
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<JobResponse> getById(@PathVariable Long jobId) {
        return ResponseEntity.ok(jobService.getById(jobId));
    }
}
