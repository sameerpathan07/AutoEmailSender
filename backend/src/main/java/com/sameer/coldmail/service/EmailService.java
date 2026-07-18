package com.sameer.coldmail.service;

import com.sameer.coldmail.entity.EmailLog;
import com.sameer.coldmail.entity.Profile;
import com.sameer.coldmail.entity.Recruiter;
import com.sameer.coldmail.repository.EmailLogRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailLogRepository emailLogRepository;

    // Sends one email to a recruiter with the resume PDF attached.
    // subject/description come pre-resolved (placeholders already substituted)
    // by the caller (RecruiterService / EmailController).
    public void sendResumeEmail(Profile profile, String toEmail, String toName,
                                 String subject, String body) throws Exception {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(toEmail);
        if (profile.getSenderEmail() != null) {
            helper.setFrom(profile.getSenderEmail());
        }
        helper.setSubject(subject);
        helper.setText(body, false);

        if (profile.getResumeData() != null && profile.getResumeData().length > 0) {
            String fileName = profile.getResumeFileName() != null
                    ? profile.getResumeFileName() : "Resume.pdf";
            helper.addAttachment(fileName, new org.springframework.core.io.ByteArrayResource(profile.getResumeData()));
        }

        try {
            mailSender.send(message);
            logResult(toName, toEmail, subject, EmailLog.Result.SUCCESS, null);
        } catch (Exception ex) {
            logResult(toName, toEmail, subject, EmailLog.Result.FAILURE, ex.getMessage());
            throw ex;
        }
    }

    private void logResult(String name, String email, String subject, EmailLog.Result result, String error) {
        EmailLog log = new EmailLog();
        log.setRecruiterName(name);
        log.setRecruiterEmail(email);
        log.setSubject(subject);
        log.setResult(result);
        log.setErrorMessage(error);
        emailLogRepository.save(log);
    }

    // Replaces {{name}} in a template with the recruiter's name, falling back to "Hiring Manager".
    public String resolvePlaceholders(String template, String name) {
        if (template == null) return "";
        String safeName = (name == null || name.isBlank()) ? "Hiring Manager" : name;
        return template.replace("{{name}}", safeName);
    }
}
