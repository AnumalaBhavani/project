package com.alumni.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contribution_history")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ContributionHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alumni_id", nullable = false)
    private AlumniProfile alumni;

    @Enumerated(EnumType.STRING)
    @Column(name = "contribution_type", nullable = false)
    private ContributionType contributionType;

    @Column(name = "reference_id")
    private Long referenceId;

    private Integer points = 0;
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist protected void onCreate() { createdAt = LocalDateTime.now(); }

    public enum ContributionType {
        JOB_POSTED, MENTORSHIP_COMPLETED, EVENT_ATTENDED, MATERIAL_UPLOADED, EVENT_ORGANIZED
    }
}
