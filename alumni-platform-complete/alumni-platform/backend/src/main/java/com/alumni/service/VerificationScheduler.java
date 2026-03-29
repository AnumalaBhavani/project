package com.alumni.service;

import com.alumni.entity.AlumniProfile;
import com.alumni.entity.Notification;
import com.alumni.repository.AlumniProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationScheduler {

    private final AlumniProfileRepository alumniProfileRepo;
    private final NotificationService notificationService;

    // Runs daily at 9 AM
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendVerificationReminders() {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        List<AlumniProfile> alumni = alumniProfileRepo.findAlumniNeedingVerification(sixMonthsAgo);
        log.info("Sending verification reminders to {} alumni", alumni.size());

        for (AlumniProfile profile : alumni) {
            if (!profile.isVerificationReminderSent()) {
                notificationService.send(
                        profile.getUser().getId(),
                        "Time to Verify Your Profile",
                        "It's been over 6 months since you last verified your profile. " +
                        "Please confirm or update your details to stay connected.",
                        Notification.NotificationType.VERIFICATION,
                        profile.getId(), "ALUMNI_PROFILE"
                );
                profile.setVerificationReminderSent(true);
                alumniProfileRepo.save(profile);
            }
        }
    }
}
