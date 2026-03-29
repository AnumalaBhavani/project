package com.alumni.dto;

import com.alumni.entity.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AlumniDTOs {

    @Data
    public static class AlumniProfileResponse {
        private Long id;
        private Long userId;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String avatarUrl;
        private String bio;
        private Integer graduationYear;
        private String degree;
        private String branch;
        private String currentCompany;
        private String currentRole;
        private String domain;
        private String location;
        private String linkedinUrl;
        private String githubUrl;
        private String websiteUrl;
        private Integer yearsOfExperience;
        private boolean availableForMentorship;
        private Integer profileCompleteness;
        private LocalDateTime lastVerifiedAt;
        private List<String> skills;
        private List<ExperienceDTO> experiences;
        private List<AchievementDTO> achievements;
        private Long totalPoints;
    }

    @Data
    public static class AlumniProfileUpdateRequest {
        private String firstName;
        private String lastName;
        private String phone;
        private String bio;
        private Integer graduationYear;
        private String degree;
        private String branch;
        private String currentCompany;
        private String currentRole;
        private String domain;
        private String location;
        private String linkedinUrl;
        private String githubUrl;
        private String websiteUrl;
        private Integer yearsOfExperience;
        private boolean availableForMentorship;
        private List<String> skills;
        private List<ExperienceDTO> experiences;
        private List<AchievementDTO> achievements;
    }

    @Data
    public static class ExperienceDTO {
        private Long id;
        private String company;
        private String role;
        private LocalDate startDate;
        private LocalDate endDate;
        private boolean isCurrent;
        private String description;
    }

    @Data
    public static class AchievementDTO {
        private Long id;
        private String title;
        private String description;
        private LocalDate achievedAt;
    }

    @Data
    public static class AlumniDirectoryItem {
        private Long id;
        private String firstName;
        private String lastName;
        private String avatarUrl;
        private String currentCompany;
        private String currentRole;
        private String domain;
        private String location;
        private Integer graduationYear;
        private boolean availableForMentorship;
        private Integer profileCompleteness;
        private List<String> skills;
    }
}

class JobDTOs {
    @Data
    public static class JobPostRequest {
        private String companyName;
        private String jobTitle;
        private String description;
        private String location;
        private Job.JobType jobType;
        private Integer experienceMin;
        private Integer experienceMax;
        private String salaryRange;
        private String domain;
        private LocalDate applicationDeadline;
        private List<String> requiredSkills;
    }

    @Data
    public static class JobResponse {
        private Long id;
        private Long postedById;
        private String postedByName;
        private String companyName;
        private String jobTitle;
        private String description;
        private String location;
        private String jobType;
        private Integer experienceMin;
        private Integer experienceMax;
        private String salaryRange;
        private String domain;
        private LocalDate applicationDeadline;
        private String status;
        private List<String> requiredSkills;
        private LocalDateTime createdAt;
        private int applicationsCount;
    }

    @Data
    public static class JobApplicationRequest {
        private String coverLetter;
        // resumeUrl will come from file upload
    }

    @Data
    public static class ApplicationResponse {
        private Long id;
        private Long jobId;
        private String jobTitle;
        private String companyName;
        private String studentName;
        private String resumeUrl;
        private BigDecimal matchScore;
        private String matchCategory;
        private String matchedSkills;
        private String status;
        private LocalDateTime appliedAt;
    }
}
