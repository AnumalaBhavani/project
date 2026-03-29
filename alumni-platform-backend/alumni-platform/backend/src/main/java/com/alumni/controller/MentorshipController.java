package com.alumni.controller;

import com.alumni.service.MentorshipService;
import com.alumni.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/mentorship")
@RequiredArgsConstructor
public class MentorshipController {

    private final MentorshipService mentorshipService;

    @PostMapping("/request/{alumniId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> sendRequest(@AuthenticationPrincipal User user,
                                          @PathVariable Long alumniId,
                                          @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(mentorshipService.sendRequest(
                user.getId(), alumniId, body.get("message"), body.get("goal")));
    }

    @PatchMapping("/request/{requestId}/respond")
    @PreAuthorize("hasRole('ALUMNI')")
    public ResponseEntity<?> respond(@AuthenticationPrincipal User user,
                                      @PathVariable Long requestId,
                                      @RequestBody Map<String, String> body) {
        LocalDateTime scheduledAt = body.get("scheduledAt") != null
                ? LocalDateTime.parse(body.get("scheduledAt")) : null;
        return ResponseEntity.ok(mentorshipService.respondToRequest(
                requestId, user.getId(), body.get("action"),
                body.get("notes"), scheduledAt, body.get("meetingLink")));
    }

    @GetMapping("/alumni/requests")
    @PreAuthorize("hasRole('ALUMNI')")
    public ResponseEntity<?> alumniRequests(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(mentorshipService.getAlumniRequests(user.getId()));
    }

    @GetMapping("/student/requests")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> studentRequests(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(mentorshipService.getStudentRequests(user.getId()));
    }
}
