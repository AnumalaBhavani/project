package com.alumni.dto;

import com.alumni.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// ---- Request DTOs ----
public class AuthDTOs {

    @Data
    public static class LoginRequest {
        @NotBlank @Email
        private String email;
        @NotBlank
        private String password;
    }

    @Data
    public static class RegisterAlumniRequest {
        @NotBlank @Email
        private String email;
        @NotBlank @Size(min = 8, message = "Password must be at least 8 characters")
        private String password;
        @NotBlank
        private String firstName;
        @NotBlank
        private String lastName;
        private Integer graduationYear;
        private String branch;
        private String degree;
    }

    @Data
    public static class RegisterStudentRequest {
        @NotBlank @Email
        private String email;
        @NotBlank @Size(min = 8)
        private String password;
        @NotBlank
        private String firstName;
        @NotBlank
        private String lastName;
        private String rollNumber;
        private Integer enrollmentYear;
        private Integer expectedGraduationYear;
        private String branch;
    }

    @Data
    public static class AuthResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType = "Bearer";
        private Long userId;
        private String email;
        private String role;
        private String firstName;
        private String lastName;
        private Long profileId;

        public AuthResponse(String accessToken, String refreshToken, User user,
                            String firstName, String lastName, Long profileId) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.userId = user.getId();
            this.email = user.getEmail();
            this.role = user.getRole().name();
            this.firstName = firstName;
            this.lastName = lastName;
            this.profileId = profileId;
        }
    }

    @Data
    public static class MessageResponse {
        private String message;
        public MessageResponse(String message) { this.message = message; }
    }
}
