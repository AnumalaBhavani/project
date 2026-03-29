package com.alumni.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "alumni_achievements")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AlumniAchievement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alumni_id")
    private AlumniProfile alumniProfile;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDate achievedAt;
}
