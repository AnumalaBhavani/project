package com.alumni.controller;

import com.alumni.entity.*;
import com.alumni.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @GetMapping("/public/list")
    public ResponseEntity<?> listJobs(
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Job.JobType jobType) {
        return ResponseEntity.ok(jobService.getApprovedJobs(domain, location, jobType));
    }

    @PostMapping("/post")
    @PreAuthorize("hasRole('ALUMNI')")
    public ResponseEntity<?> postJob(@AuthenticationPrincipal User user,
                                      @RequestBody Map<String, Object> payload) {
        Job job = new Job();
        job.setCompanyName((String) payload.get("companyName"));
        job.setJobTitle((String) payload.get("jobTitle"));
        job.setDescription((String) payload.get("description"));
        job.setLocation((String) payload.get("location"));
        job.setDomain((String) payload.get("domain"));
        job.setSalaryRange((String) payload.get("salaryRange"));
        job.setExperienceMin((Integer) payload.getOrDefault("experienceMin", 0));
        job.setExperienceMax((Integer) payload.getOrDefault("experienceMax", 5));
        if (payload.get("applicationDeadline") != null)
            job.setApplicationDeadline(LocalDate.parse((String) payload.get("applicationDeadline")));
        if (payload.get("jobType") != null)
            job.setJobType(Job.JobType.valueOf((String) payload.get("jobType")));

        List<String> skills = (List<String>) payload.getOrDefault("requiredSkills", List.of());
        return ResponseEntity.ok(jobService.postJob(user.getId(), job, skills));
    }

    @GetMapping("/my-jobs")
    @PreAuthorize("hasRole('ALUMNI')")
    public ResponseEntity<?> myJobs(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(jobService.getJobsByAlumni(user.getId()));
    }

    @PostMapping("/{jobId}/apply")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> apply(@AuthenticationPrincipal User user,
                                    @PathVariable Long jobId,
                                    @RequestParam(required = false) MultipartFile resume,
                                    @RequestParam(required = false) String coverLetter) {
        return ResponseEntity.ok(jobService.applyToJob(user.getId(), jobId, resume, coverLetter));
    }

    @GetMapping("/{jobId}/applications")
    @PreAuthorize("hasAnyRole('ALUMNI', 'ADMIN')")
    public ResponseEntity<?> getApplications(@PathVariable Long jobId) {
        return ResponseEntity.ok(jobService.getApplicationsForJob(jobId));
    }

    @GetMapping("/my-applications")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> myApplications(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(jobService.getStudentApplications(user.getId()));
    }
}
