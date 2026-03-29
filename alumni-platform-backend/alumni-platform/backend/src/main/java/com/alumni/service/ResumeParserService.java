package com.alumni.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class ResumeParserService {

    private static final List<String> KNOWN_SKILLS = List.of(
            "Java", "Python", "JavaScript", "TypeScript", "React", "Angular", "Vue",
            "Spring Boot", "Node.js", "Django", "Flask", "SQL", "MySQL", "PostgreSQL",
            "MongoDB", "Redis", "Docker", "Kubernetes", "AWS", "Azure", "GCP",
            "Machine Learning", "Deep Learning", "TensorFlow", "PyTorch", "Scikit-learn",
            "Git", "Linux", "CI/CD", "Terraform", "Ansible", "GraphQL", "REST API",
            "Microservices", "Agile", "Scrum", "System Design", "Data Structures",
            "Algorithms", "C++", "Go", "Rust", "Kotlin", "Swift", "Figma",
            "Product Management", "Data Science", "DevOps", "Cybersecurity"
    );

    public Map<String, Object> parseResume(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        try {
            String text = extractText(file);
            result.put("rawText", text);
            result.put("skills", extractSkills(text));
            result.put("jobTitle", extractJobTitle(text));
            result.put("company", extractCompany(text));
            result.put("experience", extractExperience(text));
            result.put("education", extractEducation(text));
        } catch (Exception e) {
            log.error("Error parsing resume: {}", e.getMessage());
            result.put("error", "Could not parse resume: " + e.getMessage());
        }
        return result;
    }

    private String extractText(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        if (filename != null && filename.toLowerCase().endsWith(".pdf")) {
            try (PDDocument doc = PDDocument.load(file.getInputStream())) {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(doc);
            }
        }
        // For txt/other - read raw bytes
        return new String(file.getBytes());
    }

    public List<String> extractSkills(String text) {
        List<String> found = new ArrayList<>();
        String lowerText = text.toLowerCase();
        for (String skill : KNOWN_SKILLS) {
            if (lowerText.contains(skill.toLowerCase())) {
                found.add(skill);
            }
        }
        return found;
    }

    private String extractJobTitle(String text) {
        // Common patterns for job titles
        Pattern[] patterns = {
                Pattern.compile("(?i)(?:current role|position|title)[:\\s]+([\\w\\s]+?)(?:\\n|at|@|,)", Pattern.MULTILINE),
                Pattern.compile("(?i)(?:Software Engineer|Developer|Manager|Analyst|Designer|Scientist|Engineer|Architect)[\\w\\s]*", Pattern.MULTILINE)
        };
        for (Pattern p : patterns) {
            Matcher m = p.matcher(text);
            if (m.find()) return m.group().trim();
        }
        return null;
    }

    private String extractCompany(String text) {
        Pattern p = Pattern.compile("(?i)(?:company|employer|organization|at)[:\\s]+([A-Z][\\w\\s]+?)(?:\\n|,|\\.|Ltd|Inc|Corp)", Pattern.MULTILINE);
        Matcher m = p.matcher(text);
        if (m.find()) return m.group(1).trim();
        return null;
    }

    private String extractExperience(String text) {
        Pattern p = Pattern.compile("(?i)(\\d+)[+\\s]*(?:years?|yrs?)\\s+(?:of\\s+)?experience", Pattern.MULTILINE);
        Matcher m = p.matcher(text);
        if (m.find()) return m.group(1) + " years";
        return null;
    }

    private String extractEducation(String text) {
        Pattern p = Pattern.compile("(?i)(B\\.?Tech|M\\.?Tech|B\\.?E|M\\.?E|MBA|MCA|BCA|B\\.?Sc|M\\.?Sc|PhD)[\\w\\s,]*", Pattern.MULTILINE);
        Matcher m = p.matcher(text);
        if (m.find()) return m.group().trim();
        return null;
    }

    /**
     * Compute match score between student resume skills and job required skills.
     */
    public double computeMatchScore(List<String> studentSkills, List<String> requiredSkills) {
        if (requiredSkills == null || requiredSkills.isEmpty()) return 0;
        long matched = studentSkills.stream()
                .filter(s -> requiredSkills.stream()
                        .anyMatch(r -> r.equalsIgnoreCase(s)))
                .count();
        return ((double) matched / requiredSkills.size()) * 100;
    }

    public List<String> getMatchedSkills(List<String> studentSkills, List<String> requiredSkills) {
        return studentSkills.stream()
                .filter(s -> requiredSkills.stream()
                        .anyMatch(r -> r.equalsIgnoreCase(s)))
                .toList();
    }

    public String getMatchCategory(double score) {
        if (score >= 70) return "HIGH";
        if (score >= 40) return "MODERATE";
        return "LOW";
    }
}
