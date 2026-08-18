package com.alumniportal.service;

import com.alumniportal.dto.JobRequest;
import com.alumniportal.dto.JobResponse;
import com.alumniportal.entity.*;
import com.alumniportal.exception.ApiException;
import com.alumniportal.repository.JobApplicationRepository;
import com.alumniportal.repository.JobRepository;
import com.alumniportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final ContributionService contributionService;

    @Transactional
    public JobResponse postJob(Long alumniUserId, JobRequest request) {
        User alumni = userRepository.findById(alumniUserId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        Job job = Job.builder()
                .postedBy(alumni)
                .companyName(request.getCompanyName())
                .title(request.getTitle())
                .description(request.getDescription())
                .requiredSkills(request.getRequiredSkills())
                .experienceRequired(request.getExperienceRequired())
                .location(request.getLocation())
                .applyLink(request.getApplyLink())
                .type(request.getType() != null ? request.getType() : JobType.JOB)
                .status(JobStatus.PENDING)
                .build();

        job = jobRepository.save(job);
        return toResponse(job);
    }

    public List<JobResponse> listApproved() {
        return jobRepository.findByStatus(JobStatus.APPROVED).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<JobResponse> listPending() {
        return jobRepository.findByStatus(JobStatus.PENDING).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<JobResponse> listByAlumni(Long alumniUserId) {
        return jobRepository.findByPostedById(alumniUserId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public JobResponse getById(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ApiException("Job not found", HttpStatus.NOT_FOUND));
        return toResponse(job);
    }

    Job getEntity(Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new ApiException("Job not found", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public JobResponse setStatus(Long jobId, JobStatus status) {
        Job job = getEntity(jobId);
        job.setStatus(status);
        jobRepository.save(job);

        if (status == JobStatus.APPROVED) {
            contributionService.recordContribution(job.getPostedBy().getId(), ContributionType.JOB_POSTED, job.getId());
        }
        return toResponse(job);
    }

    public long countByStatus(JobStatus status) {
        return jobRepository.countByStatus(status);
    }

    private JobResponse toResponse(Job job) {
        long applicantCount = jobApplicationRepository.findByJobIdOrderByMatchScoreDesc(job.getId()).size();
        return JobResponse.builder()
                .id(job.getId())
                .postedById(job.getPostedBy().getId())
                .postedByName(job.getPostedBy().getName())
                .companyName(job.getCompanyName())
                .title(job.getTitle())
                .description(job.getDescription())
                .requiredSkills(job.getRequiredSkills())
                .experienceRequired(job.getExperienceRequired())
                .location(job.getLocation())
                .applyLink(job.getApplyLink())
                .type(job.getType())
                .status(job.getStatus())
                .createdAt(job.getCreatedAt())
                .applicantCount(applicantCount)
                .build();
    }
}
