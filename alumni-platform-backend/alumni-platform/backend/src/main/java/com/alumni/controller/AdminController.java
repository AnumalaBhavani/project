package com.alumni.controller;

import com.alumni.entity.*;
import com.alumni.repository.*;
import com.alumni.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepo;
    private final AlumniProfileRepository alumniRepo;
    private final JobRepository jobRepo;
    private final MentorshipRequestRepository mentorshipRepo;
    private final ContributionHistoryRepository contributionRepo;
    private final EventRepository eventRepo;
    private final JobService jobService;

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalAlumni", userRepo.countByRole(User.Role.ALUMNI));
        stats.put("activeAlumni", userRepo.countByRoleAndActive(User.Role.ALUMNI, true));
        stats.put("totalStudents", userRepo.countByRole(User.Role.STUDENT));
        stats.put("totalJobs", jobRepo.count());
        stats.put("approvedJobs", jobRepo.countByStatus(Job.JobStatus.APPROVED));
        stats.put("pendingJobs", jobRepo.countByStatus(Job.JobStatus.PENDING));
        stats.put("mentorshipSessions", mentorshipRepo.countCompletedSessions());
        stats.put("averageCompleteness", alumniRepo.findAverageProfileCompleteness());
        stats.put("pendingApprovals", alumniRepo.findPendingApprovals().size());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/alumni/pending")
    public ResponseEntity<?> getPendingAlumni() {
        return ResponseEntity.ok(alumniRepo.findPendingApprovals());
    }

    @PatchMapping("/alumni/{userId}/approve")
    public ResponseEntity<?> approveAlumni(@PathVariable Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(true);
        user.setVerified(true);
        userRepo.save(user);
        return ResponseEntity.ok(Map.of("message", "Alumni approved"));
    }

    @GetMapping("/jobs/pending")
    public ResponseEntity<?> getPendingJobs() {
        return ResponseEntity.ok(jobService.getPendingJobs());
    }

    @PatchMapping("/jobs/{jobId}/moderate")
    public ResponseEntity<?> moderateJob(@PathVariable Long jobId,
                                          @RequestParam String action) {
        return ResponseEntity.ok(jobService.moderateJob(jobId, action));
    }

    @GetMapping("/alumni/incomplete-profiles")
    public ResponseEntity<?> incompleteProfiles(@RequestParam(defaultValue = "70") int threshold) {
        return ResponseEntity.ok(alumniRepo.findIncompleteProfiles(threshold));
    }

    @GetMapping("/contributors/top")
    public ResponseEntity<?> topContributors() {
        return ResponseEntity.ok(contributionRepo.findTopContributors());
    }

    @GetMapping("/alumni/all")
    public ResponseEntity<?> allAlumni() {
        return ResponseEntity.ok(alumniRepo.findAll());
    }
}
