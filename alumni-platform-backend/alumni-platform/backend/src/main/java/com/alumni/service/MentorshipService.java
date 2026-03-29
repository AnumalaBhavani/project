package com.alumni.service;

import com.alumni.entity.*;
import com.alumni.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MentorshipService {

    private final MentorshipRequestRepository mentorshipRepo;
    private final AlumniProfileRepository alumniProfileRepo;
    private final StudentProfileRepository studentProfileRepo;
    private final NotificationService notificationService;
    private final ContributionHistoryRepository contributionRepo;

    @Transactional
    public MentorshipRequest sendRequest(Long studentUserId, Long alumniId, String message, String goal) {
        StudentProfile student = studentProfileRepo.findByUserId(studentUserId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        AlumniProfile alumni = alumniProfileRepo.findById(alumniId)
                .orElseThrow(() -> new RuntimeException("Alumni not found"));

        if (!alumni.isAvailableForMentorship()) {
            throw new RuntimeException("Alumni is not available for mentorship");
        }

        MentorshipRequest request = MentorshipRequest.builder()
                .student(student).alumni(alumni)
                .message(message).goal(goal)
                .status(MentorshipRequest.RequestStatus.PENDING)
                .build();
        request = mentorshipRepo.save(request);

        notificationService.send(alumni.getUser().getId(),
                "New Mentorship Request",
                student.getFirstName() + " " + student.getLastName() + " sent you a mentorship request.",
                Notification.NotificationType.MENTORSHIP, request.getId(), "MENTORSHIP_REQUEST");

        return request;
    }

    @Transactional
    public MentorshipRequest respondToRequest(Long requestId, Long alumniUserId,
                                               String action, String notes,
                                               LocalDateTime scheduledAt, String meetingLink) {
        MentorshipRequest request = mentorshipRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        AlumniProfile alumni = alumniProfileRepo.findByUserId(alumniUserId)
                .orElseThrow(() -> new RuntimeException("Alumni not found"));

        if (!request.getAlumni().getId().equals(alumni.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        switch (action.toUpperCase()) {
            case "ACCEPT" -> request.setStatus(MentorshipRequest.RequestStatus.ACCEPTED);
            case "REJECT" -> request.setStatus(MentorshipRequest.RequestStatus.REJECTED);
            case "SCHEDULE" -> {
                request.setStatus(MentorshipRequest.RequestStatus.SCHEDULED);
                request.setScheduledAt(scheduledAt);
                request.setMeetingLink(meetingLink);
            }
            case "COMPLETE" -> {
                request.setStatus(MentorshipRequest.RequestStatus.COMPLETED);
                contributionRepo.save(ContributionHistory.builder()
                        .alumni(alumni)
                        .contributionType(ContributionHistory.ContributionType.MENTORSHIP_COMPLETED)
                        .points(20)
                        .description("Completed mentorship session with " +
                                request.getStudent().getFirstName())
                        .build());
            }
        }
        request.setAlumniNotes(notes);
        request = mentorshipRepo.save(request);

        notificationService.send(request.getStudent().getUser().getId(),
                "Mentorship Request " + request.getStatus(),
                alumni.getFirstName() + " " + alumni.getLastName() +
                        " has " + action.toLowerCase() + "d your mentorship request.",
                Notification.NotificationType.MENTORSHIP, requestId, "MENTORSHIP_REQUEST");

        return request;
    }

    public List<MentorshipRequest> getAlumniRequests(Long alumniUserId) {
        AlumniProfile alumni = alumniProfileRepo.findByUserId(alumniUserId)
                .orElseThrow(() -> new RuntimeException("Alumni not found"));
        return mentorshipRepo.findByAlumniId(alumni.getId());
    }

    public List<MentorshipRequest> getStudentRequests(Long studentUserId) {
        StudentProfile student = studentProfileRepo.findByUserId(studentUserId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return mentorshipRepo.findByStudentId(student.getId());
    }
}
