package com.alumniportal.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.enabled}")
    private boolean mailEnabled;

    public void sendVerificationReminder(String toEmail, String name) {
        String subject = "Please confirm your Alumni Portal profile";
        String body = "Hi " + name + ",\n\n"
                + "It's been a while since you last verified your profile on the Alumni Portal. "
                + "Please log in and confirm or update your details so students and fellow alumni "
                + "always see accurate information.\n\n"
                + "Thanks,\nAlumni Portal Team";
        send(toEmail, subject, body);
    }

    public void sendMentorshipNotification(String toEmail, String name, String message) {
        send(toEmail, "Mentorship update on Alumni Portal", "Hi " + name + ",\n\n" + message + "\n\nAlumni Portal Team");
    }

    private void send(String toEmail, String subject, String body) {
        if (!mailEnabled) {
            // SMTP not configured (see application.properties: app.mail.enabled).
            // We log instead of throwing so the rest of the app keeps working out of the box.
            log.info("[MOCK EMAIL] To: {} | Subject: {} | Body: {}", toEmail, subject, body);
            return;
        }
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, false);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }
}
