package com.alumni.service;

import com.alumni.entity.AlumniProfile;
import org.springframework.stereotype.Service;

@Service
public class ProfileCompletenessService {

    /**
     * Calculates profile completeness as a percentage.
     * Fields: company(15), skills(20), location(10), experience(15), domain(15), bio(15), role(10)
     */
    public int calculate(AlumniProfile profile) {
        int score = 0;
        int total = 100;

        if (hasValue(profile.getCurrentCompany())) score += 15;
        if (profile.getSkills() != null && !profile.getSkills().isEmpty()) score += 20;
        if (hasValue(profile.getLocation())) score += 10;
        if (profile.getExperiences() != null && !profile.getExperiences().isEmpty()) score += 15;
        if (hasValue(profile.getDomain())) score += 15;
        if (hasValue(profile.getBio())) score += 15;
        if (hasValue(profile.getCurrentRole())) score += 10;

        return Math.min(score, total);
    }

    /**
     * Returns suggestions for incomplete fields.
     */
    public String getSuggestions(AlumniProfile profile) {
        StringBuilder sb = new StringBuilder();
        if (!hasValue(profile.getCurrentCompany())) sb.append("Add your current company. ");
        if (profile.getSkills() == null || profile.getSkills().isEmpty()) sb.append("Add your skills. ");
        if (!hasValue(profile.getLocation())) sb.append("Add your location. ");
        if (profile.getExperiences() == null || profile.getExperiences().isEmpty()) sb.append("Add work experience. ");
        if (!hasValue(profile.getDomain())) sb.append("Add your domain. ");
        if (!hasValue(profile.getBio())) sb.append("Write a short bio. ");
        if (!hasValue(profile.getCurrentRole())) sb.append("Add your current role. ");
        return sb.toString().trim();
    }

    private boolean hasValue(String s) {
        return s != null && !s.isBlank();
    }
}
