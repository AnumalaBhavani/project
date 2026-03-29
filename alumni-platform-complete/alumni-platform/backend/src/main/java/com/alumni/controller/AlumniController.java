package com.alumni.controller;

import com.alumni.entity.*;
import com.alumni.repository.*;
import com.alumni.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/alumni")
@RequiredArgsConstructor
public class AlumniController {

    private final AlumniProfileRepository alumniProfileRepo;
    private final ProfileCompletenessService completenessService;
    private final ResumeParserService resumeParserService;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;
    private final ContributionHistoryRepository contributionRepo;

    @GetMapping("/directory")
    public ResponseEntity<?> getDirectory(
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer graduationYear,
            @RequestParam(required = false) Boolean mentorship,
            @RequestParam(required = false) String skill) {
        List<AlumniProfile> alumni = alumniProfileRepo.searchAlumni(company, domain, location, graduationYear, mentorship);
        if (skill != null && !skill.isBlank()) {
            String s = skill.toLowerCase();
            alumni = alumni.stream()
                    .filter(a -> a.getSkills().stream().anyMatch(sk -> sk.getSkill().toLowerCase().contains(s)))
                    .toList();
        }
        return ResponseEntity.ok(alumni);
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('ALUMNI')")
    public ResponseEntity<?> getMyProfile(@AuthenticationPrincipal User user) {
        AlumniProfile profile = alumniProfileRepo.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        return ResponseEntity.ok(enrichProfile(profile));
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<?> getProfileById(@PathVariable Long id) {
        AlumniProfile profile = alumniProfileRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        return ResponseEntity.ok(enrichProfile(profile));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('ALUMNI')")
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal User user,
                                            @RequestBody Map<String, Object> updates) {
        AlumniProfile profile = alumniProfileRepo.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        if (updates.containsKey("firstName")) profile.setFirstName((String) updates.get("firstName"));
        if (updates.containsKey("lastName")) profile.setLastName((String) updates.get("lastName"));
        if (updates.containsKey("bio")) profile.setBio((String) updates.get("bio"));
        if (updates.containsKey("currentCompany")) profile.setCurrentCompany((String) updates.get("currentCompany"));
        if (updates.containsKey("currentRole")) profile.setCurrentRole((String) updates.get("currentRole"));
        if (updates.containsKey("domain")) profile.setDomain((String) updates.get("domain"));
        if (updates.containsKey("location")) profile.setLocation((String) updates.get("location"));
        if (updates.containsKey("linkedinUrl")) profile.setLinkedinUrl((String) updates.get("linkedinUrl"));
        if (updates.containsKey("yearsOfExperience"))
            profile.setYearsOfExperience((Integer) updates.get("yearsOfExperience"));
        if (updates.containsKey("availableForMentorship"))
            profile.setAvailableForMentorship((Boolean) updates.get("availableForMentorship"));

        // Update skills if provided
        if (updates.containsKey("skills")) {
            List<String> skillList = (List<String>) updates.get("skills");
            profile.getSkills().clear();
            skillList.forEach(s -> profile.getSkills().add(
                    AlumniSkill.builder().alumniProfile(profile).skill(s).build()));
        }

        int completeness = completenessService.calculate(profile);
        profile.setProfileCompleteness(completeness);

        alumniProfileRepo.save(profile);
        return ResponseEntity.ok(enrichProfile(profile));
    }

    @PostMapping("/profile/verify")
    @PreAuthorize("hasRole('ALUMNI')")
    public ResponseEntity<?> verifyProfile(@AuthenticationPrincipal User user) {
        AlumniProfile profile = alumniProfileRepo.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        profile.setLastVerifiedAt(LocalDateTime.now());
        profile.setVerificationReminderSent(false);
        alumniProfileRepo.save(profile);
        return ResponseEntity.ok(Map.of("message", "Profile verified successfully"));
    }

    @PostMapping("/profile/parse-resume")
    @PreAuthorize("hasRole('ALUMNI')")
    public ResponseEntity<?> parseResume(@AuthenticationPrincipal User user,
                                          @RequestParam MultipartFile file) {
        Map<String, Object> parsed = resumeParserService.parseResume(file);
        String url = fileStorageService.storeFile(file, "resumes");
        parsed.put("resumeUrl", url);
        return ResponseEntity.ok(parsed);
    }

    @PatchMapping("/profile/mentorship")
    @PreAuthorize("hasRole('ALUMNI')")
    public ResponseEntity<?> toggleMentorship(@AuthenticationPrincipal User user,
                                               @RequestParam boolean available) {
        AlumniProfile profile = alumniProfileRepo.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        profile.setAvailableForMentorship(available);
        alumniProfileRepo.save(profile);
        return ResponseEntity.ok(Map.of("availableForMentorship", available));
    }

    @GetMapping("/contributions")
    @PreAuthorize("hasRole('ALUMNI')")
    public ResponseEntity<?> getContributions(@AuthenticationPrincipal User user) {
        AlumniProfile profile = alumniProfileRepo.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        return ResponseEntity.ok(contributionRepo.findByAlumniIdOrderByCreatedAtDesc(profile.getId()));
    }

    @GetMapping("/notifications")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getNotifications(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(notificationService.getUserNotifications(user.getId()));
    }

    @PostMapping("/notifications/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> markAllRead(@AuthenticationPrincipal User user) {
        notificationService.markAllRead(user.getId());
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
    }

    private Map<String, Object> enrichProfile(AlumniProfile p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("userId", p.getUser().getId());
        m.put("email", p.getUser().getEmail());
        m.put("firstName", p.getFirstName());
        m.put("lastName", p.getLastName());
        m.put("phone", p.getPhone());
        m.put("bio", p.getBio());
        m.put("graduationYear", p.getGraduationYear());
        m.put("degree", p.getDegree());
        m.put("branch", p.getBranch());
        m.put("currentCompany", p.getCurrentCompany());
        m.put("currentRole", p.getCurrentRole());
        m.put("domain", p.getDomain());
        m.put("location", p.getLocation());
        m.put("linkedinUrl", p.getLinkedinUrl());
        m.put("yearsOfExperience", p.getYearsOfExperience());
        m.put("availableForMentorship", p.isAvailableForMentorship());
        m.put("profileCompleteness", p.getProfileCompleteness());
        m.put("suggestions", completenessService.getSuggestions(p));
        m.put("lastVerifiedAt", p.getLastVerifiedAt());
        m.put("skills", p.getSkills().stream().map(AlumniSkill::getSkill).toList());
        m.put("totalPoints", contributionRepo.sumPointsByAlumniId(p.getId()));
        return m;
    }
}
