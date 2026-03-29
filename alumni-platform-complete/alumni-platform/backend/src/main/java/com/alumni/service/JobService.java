package com.alumni.service;

import com.alumni.entity.*;
import com.alumni.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final JobApplicationRepository applicationRepository;
    private final AlumniProfileRepository alumniProfileRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final ResumeParserService resumeParserService;
    private final FileStorageService fileStorageService;
    private final ContributionHistoryRepository contributionRepo;
    private final NotificationService notificationService;

    @Transactional
    public Job postJob(Long alumniUserId, Job jobRequest, List<String> skills) {
        AlumniProfile alumni = alumniProfileRepository.findByUserId(alumniUserId)
                .orElseThrow(() -> new RuntimeException("Alumni profile not found"));

        jobRequest.setPostedBy(alumni);
        jobRequest.setStatus(Job.JobStatus.PENDING);

        List<JobRequiredSkill> requiredSkills = skills.stream()
                .map(s -> JobRequiredSkill.builder().job(jobRequest).skill(s).build())
                .toList();
        jobRequest.setRequiredSkills(requiredSkills);

        Job saved = jobRepository.save(jobRequest);

        // Track contribution
        contributionRepo.save(ContributionHistory.builder()
                .alumni(alumni)
                .contributionType(ContributionHistory.ContributionType.JOB_POSTED)
                .referenceId(saved.getId())
                .points(10)
                .description("Posted job: " + saved.getJobTitle() + " at " + saved.getCompanyName())
                .build());

        return saved;
    }

    public List<Job> getApprovedJobs(String domain, String location, Job.JobType jobType) {
        return jobRepository.searchApprovedJobs(domain, location, jobType);
    }

    public List<Job> getJobsByAlumni(Long alumniUserId) {
        AlumniProfile alumni = alumniProfileRepository.findByUserId(alumniUserId)
                .orElseThrow(() -> new RuntimeException("Alumni profile not found"));
        return jobRepository.findByPostedById(alumni.getId());
    }

    public List<Job> getPendingJobs() {
        return jobRepository.findByStatus(Job.JobStatus.PENDING);
    }

    @Transactional
    public Job moderateJob(Long jobId, String action) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        job.setStatus("APPROVE".equalsIgnoreCase(action) ? Job.JobStatus.APPROVED : Job.JobStatus.REJECTED);
        return jobRepository.save(job);
    }

    @Transactional
    public JobApplication applyToJob(Long studentUserId, Long jobId,
                                     MultipartFile resumeFile, String coverLetter) {
        StudentProfile student = studentProfileRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new RuntimeException("Student profile not found"));
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (applicationRepository.existsByJobIdAndStudentId(jobId, student.getId())) {
            throw new RuntimeException("Already applied to this job");
        }

        // Store resume
        String resumeUrl = null;
        List<String> studentSkills = List.of();
        if (resumeFile != null && !resumeFile.isEmpty()) {
            resumeUrl = fileStorageService.storeFile(resumeFile, "resumes");
            var parsed = resumeParserService.parseResume(resumeFile);
            studentSkills = (List<String>) parsed.getOrDefault("skills", List.of());
        } else if (student.getSkills() != null) {
            studentSkills = Arrays.stream(student.getSkills().split(","))
                    .map(String::trim).toList();
        }

        // Calculate match score
        List<String> requiredSkills = job.getRequiredSkills().stream()
                .map(JobRequiredSkill::getSkill).toList();
        double matchScore = resumeParserService.computeMatchScore(studentSkills, requiredSkills);
        List<String> matchedSkills = resumeParserService.getMatchedSkills(studentSkills, requiredSkills);
        String category = resumeParserService.getMatchCategory(matchScore);

        JobApplication application = JobApplication.builder()
                .job(job)
                .student(student)
                .resumeUrl(resumeUrl != null ? resumeUrl : student.getResumeUrl())
                .coverLetter(coverLetter)
                .matchScore(BigDecimal.valueOf(matchScore))
                .matchCategory(JobApplication.MatchCategory.valueOf(category))
                .matchedSkills(String.join(", ", matchedSkills))
                .status(JobApplication.ApplicationStatus.APPLIED)
                .build();

        return applicationRepository.save(application);
    }

    public List<JobApplication> getApplicationsForJob(Long jobId) {
        return applicationRepository.findByJobIdOrderByMatchScoreDesc(jobId);
    }

    public List<JobApplication> getStudentApplications(Long studentUserId) {
        StudentProfile student = studentProfileRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new RuntimeException("Student profile not found"));
        return applicationRepository.findByStudentId(student.getId());
    }
}
