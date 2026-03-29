package com.alumni.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "alumni_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlumniProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    private String phone;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "graduation_year")
    private Integer graduationYear;

    private String degree;
    private String branch;

    @Column(name = "current_company")
    private String currentCompany;

    @Column(name = "current_role")
    private String currentRole;

    private String domain;
    private String location;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Column(name = "github_url")
    private String githubUrl;

    @Column(name = "website_url")
    private String websiteUrl;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience = 0;

    @Column(name = "available_for_mentorship")
    private boolean availableForMentorship = false;

    @Column(name = "profile_completeness")
    private Integer profileCompleteness = 0;

    @Column(name = "last_verified_at")
    private LocalDateTime lastVerifiedAt;

    @Column(name = "verification_reminder_sent")
    private boolean verificationReminderSent = false;

    @Column(name = "resume_url")
    private String resumeUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "alumniProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AlumniSkill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "alumniProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AlumniExperience> experiences = new ArrayList<>();

    @OneToMany(mappedBy = "alumniProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AlumniAchievement> achievements = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
