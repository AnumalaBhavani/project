package com.alumni.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_applications")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class JobApplication {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    @Column(name = "resume_url")
    private String resumeUrl;

    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;

    @Column(name = "match_score")
    private BigDecimal matchScore = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_category")
    private MatchCategory matchCategory = MatchCategory.LOW;

    @Column(name = "matched_skills", columnDefinition = "TEXT")
    private String matchedSkills;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist protected void onCreate() { appliedAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public enum MatchCategory { HIGH, MODERATE, LOW }
    public enum ApplicationStatus { APPLIED, SHORTLISTED, INTERVIEWED, SELECTED, REJECTED }
}
