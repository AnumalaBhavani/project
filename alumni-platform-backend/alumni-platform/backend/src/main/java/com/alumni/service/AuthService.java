package com.alumni.service;

import com.alumni.dto.AuthDTOs.*;
import com.alumni.entity.*;
import com.alumni.repository.*;
import com.alumni.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final AlumniProfileRepository alumniProfileRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final ProfileCompletenessService profileCompletenessService;

    @Transactional
    public AuthResponse registerAlumni(RegisterAlumniRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.ALUMNI)
                .active(false)  // requires admin approval
                .verified(false)
                .build();
        user = userRepository.save(user);

        AlumniProfile profile = AlumniProfile.builder()
                .user(user)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .graduationYear(request.getGraduationYear())
                .branch(request.getBranch())
                .degree(request.getDegree())
                .profileCompleteness(0)
                .build();
        profile = alumniProfileRepository.save(profile);

        int completeness = profileCompletenessService.calculate(profile);
        profile.setProfileCompleteness(completeness);
        alumniProfileRepository.save(profile);

        // For demo, auto-activate (remove in production - require admin approval)
        user.setActive(true);
        user.setVerified(true);
        userRepository.save(user);

        String accessToken = jwtUtils.generateToken(user);
        String refreshToken = jwtUtils.generateRefreshToken(user);
        return new AuthResponse(accessToken, refreshToken, user,
                profile.getFirstName(), profile.getLastName(), profile.getId());
    }

    @Transactional
    public AuthResponse registerStudent(RegisterStudentRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.STUDENT)
                .active(true)
                .verified(true)
                .build();
        user = userRepository.save(user);

        StudentProfile profile = StudentProfile.builder()
                .user(user)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .rollNumber(request.getRollNumber())
                .enrollmentYear(request.getEnrollmentYear())
                .expectedGraduationYear(request.getExpectedGraduationYear())
                .branch(request.getBranch())
                .build();
        profile = studentProfileRepository.save(profile);

        String accessToken = jwtUtils.generateToken(user);
        String refreshToken = jwtUtils.generateRefreshToken(user);
        return new AuthResponse(accessToken, refreshToken, user,
                profile.getFirstName(), profile.getLastName(), profile.getId());
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = (User) authentication.getPrincipal();

        String accessToken = jwtUtils.generateToken(user);
        String refreshToken = jwtUtils.generateRefreshToken(user);

        String firstName = "", lastName = "";
        Long profileId = null;

        if (user.getRole() == User.Role.ALUMNI) {
            var profile = alumniProfileRepository.findByUser(user);
            if (profile.isPresent()) {
                firstName = profile.get().getFirstName();
                lastName = profile.get().getLastName();
                profileId = profile.get().getId();
            }
        } else if (user.getRole() == User.Role.STUDENT) {
            var profile = studentProfileRepository.findByUser(user);
            if (profile.isPresent()) {
                firstName = profile.get().getFirstName();
                lastName = profile.get().getLastName();
                profileId = profile.get().getId();
            }
        } else {
            firstName = "Admin";
            lastName = "User";
        }

        return new AuthResponse(accessToken, refreshToken, user, firstName, lastName, profileId);
    }
}
