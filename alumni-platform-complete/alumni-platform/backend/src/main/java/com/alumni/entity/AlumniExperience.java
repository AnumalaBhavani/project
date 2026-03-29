package com.alumni.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "alumni_experience")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AlumniExperience {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alumni_id")
    private AlumniProfile alumniProfile;

    private String company;
    private String role;
    private LocalDate startDate;
    private LocalDate endDate;

    @Column(name = "is_current")
    private boolean isCurrent = false;

    @Column(columnDefinition = "TEXT")
    private String description;
}
