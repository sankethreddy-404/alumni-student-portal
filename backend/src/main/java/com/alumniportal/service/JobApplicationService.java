package com.alumniportal.service;

import com.alumniportal.dto.JobApplicationResponse;
import com.alumniportal.entity.*;
import com.alumniportal.exception.ApiException;
import com.alumniportal.repository.JobApplicationRepository;
import com.alumniportal.repository.JobRepository;
import com.alumniportal.repository.ResumeDataRepository;
import com.alumniportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ResumeDataRepository resumeDataRepository;
    private final ResumeParsingService resumeParsingService;
    private final SkillMatchingService skillMatchingService;
    private final FileStorageService fileStorageService;

    @Transactional
    public JobApplicationResponse apply(Long studentId, Long jobId, MultipartFile resumeFile) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ApiException("Job not found", HttpStatus.NOT_FOUND));

        if (job.getStatus() != JobStatus.APPROVED) {
            throw new ApiException("This job is not currently accepting applications", HttpStatus.BAD_REQUEST);
        }
        if (jobApplicationRepository.existsByJobIdAndStudentId(jobId, studentId)) {
            throw new ApiException("You have already applied to this job", HttpStatus.CONFLICT);
        }

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        String resumePath = fileStorageService.store(resumeFile, "resumes");
        String rawText = resumeParsingService.extractText(resumeFile);
        List<String> extractedSkills = resumeParsingService.extractSkills(rawText);
        String skillsCsv = resumeParsingService.joinSkills(extractedSkills);

        ResumeData resumeData = ResumeData.builder()
                .user(student)
                .rawText(rawText)
                .extractedSkills(skillsCsv)
                .extractedCompany(resumeParsingService.extractCompany(rawText))
                .extractedRole(resumeParsingService.extractRole(rawText))
                .extractedExperience(resumeParsingService.extractExperienceYears(rawText))
                .source(ResumeSource.RESUME_UPLOAD)
                .build();
        resumeDataRepository.save(resumeData);

        double score = skillMatchingService.computeMatchScore(skillsCsv, job.getRequiredSkills());
        MatchCategory category = skillMatchingService.categorize(score);

        JobApplication application = JobApplication.builder()
                .job(job)
                .student(student)
                .resumeFilePath(resumePath)
                .matchScore(score)
                .matchCategory(category)
                .status(ApplicationStatus.APPLIED)
                .build();

        application = jobApplicationRepository.save(application);
        recalculateRanks(jobId);

        return toResponse(jobApplicationRepository.findById(application.getId()).orElseThrow());
    }

    @Transactional
    public void recalculateRanks(Long jobId) {
        List<JobApplication> applications = jobApplicationRepository.findByJobIdOrderByMatchScoreDesc(jobId);
        int rank = 1;
        for (JobApplication app : applications) {
            app.setRank(rank++);
        }
        jobApplicationRepository.saveAll(applications);
    }

    public List<JobApplicationResponse> getRankedApplicants(Long jobId) {
        return jobApplicationRepository.findByJobIdOrderByMatchScoreDesc(jobId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<JobApplicationResponse> getMyApplications(Long studentId) {
        return jobApplicationRepository.findByStudentIdOrderByAppliedAtDesc(studentId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public JobApplicationResponse shortlist(Long applicationId) {
        JobApplication app = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApiException("Application not found", HttpStatus.NOT_FOUND));
        app.setStatus(ApplicationStatus.SHORTLISTED);
        jobApplicationRepository.save(app);
        return toResponse(app);
    }

    @Transactional
    public JobApplicationResponse refer(Long applicationId) {
        JobApplication app = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApiException("Application not found", HttpStatus.NOT_FOUND));
        app.setStatus(ApplicationStatus.REFERRED);
        jobApplicationRepository.save(app);
        return toResponse(app);
    }

    @Transactional
    public JobApplicationResponse reject(Long applicationId) {
        JobApplication app = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApiException("Application not found", HttpStatus.NOT_FOUND));
        app.setStatus(ApplicationStatus.REJECTED);
        jobApplicationRepository.save(app);
        return toResponse(app);
    }

    /** Returns the top N ranked candidates for a job, marking them SHORTLISTED. */
    @Transactional
    public List<JobApplicationResponse> autoShortlistTop(Long jobId, int topN) {
        List<JobApplication> ranked = jobApplicationRepository.findByJobIdOrderByMatchScoreDesc(jobId);
        List<JobApplication> top = ranked.stream().limit(topN).collect(Collectors.toList());
        for (JobApplication app : top) {
            if (app.getStatus() == ApplicationStatus.APPLIED) {
                app.setStatus(ApplicationStatus.SHORTLISTED);
            }
        }
        jobApplicationRepository.saveAll(top);
        return top.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private JobApplicationResponse toResponse(JobApplication app) {
        return JobApplicationResponse.builder()
                .id(app.getId())
                .jobId(app.getJob().getId())
                .jobTitle(app.getJob().getTitle())
                .companyName(app.getJob().getCompanyName())
                .studentId(app.getStudent().getId())
                .studentName(app.getStudent().getName())
                .studentEmail(app.getStudent().getEmail())
                .resumeFilePath(app.getResumeFilePath())
                .matchScore(app.getMatchScore())
                .matchCategory(app.getMatchCategory())
                .status(app.getStatus())
                .rank(app.getRank())
                .appliedAt(app.getAppliedAt())
                .build();
    }
}
