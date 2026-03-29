package com.alumni.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "alumni_skills")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlumniSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alumni_id")
    private AlumniProfile alumniProfile;

    private String skill;
}
