package com.alumniportal.scheduler;

import com.alumniportal.entity.AlumniProfile;
import com.alumniportal.entity.ProfileVerificationLog;
import com.alumniportal.repository.AlumniProfileRepository;
import com.alumniportal.repository.ProfileVerificationLogRepository;
import com.alumniportal.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfileVerificationScheduler {

    private final AlumniProfileRepository alumniProfileRepository;
    private final ProfileVerificationLogRepository verificationLogRepository;
    private final NotificationService notificationService;

    @Value("${app.profile.verification-interval-days}")
    private int verificationIntervalDays;

    /**
    * Sends verification reminders for stale alumni profiles.
    */
    @Scheduled(cron = "0 0 2 * * *")
    public void sendPeriodicVerificationReminders() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(verificationIntervalDays);
        List<AlumniProfile> dueForVerification = alumniProfileRepository.findUnverifiedSince(cutoff);

        log.info("Profile verification scheduler: {} alumni profiles due for reminder", dueForVerification.size());

        for (AlumniProfile profile : dueForVerification) {
            try {
                notificationService.sendVerificationReminder(
                        profile.getUser().getEmail(), profile.getUser().getName());

                ProfileVerificationLog logEntry = ProfileVerificationLog.builder()
                        .alumni(profile.getUser())
                        .remindedAt(LocalDateTime.now())
                        .build();
                verificationLogRepository.save(logEntry);
            } catch (Exception e) {
                log.error("Failed to send verification reminder to {}: {}",
                        profile.getUser().getEmail(), e.getMessage());
            }
        }
    }
}
