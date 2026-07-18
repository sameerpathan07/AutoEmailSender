package com.sameer.coldmail.controller;

import com.sameer.coldmail.dto.EmailSendResult;
import com.sameer.coldmail.dto.QuotaStatus;
import com.sameer.coldmail.dto.SendEmailRequest;
import com.sameer.coldmail.entity.EmailLog;
import com.sameer.coldmail.entity.Profile;
import com.sameer.coldmail.entity.Recruiter;
import com.sameer.coldmail.repository.EmailLogRepository;
import com.sameer.coldmail.repository.RecruiterRepository;
import com.sameer.coldmail.service.DailyLimitService;
import com.sameer.coldmail.service.EmailService;
import com.sameer.coldmail.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;
    private final ProfileService profileService;
    private final EmailLogRepository emailLogRepository;
    private final RecruiterRepository recruiterRepository;
    private final DailyLimitService dailyLimitService;

    // This is what the Home page single-send form calls.
    // Name + email come from the form; subject/description default to the
    // profile's templates unless the user overrode them on the form.
    @PostMapping("/send")
    public EmailSendResult sendSingle(@Valid @RequestBody SendEmailRequest request) {

        // 1. Duplicate check - has this email already been contacted?
        var existing = recruiterRepository.findByEmailIgnoreCase(request.getEmail());
        if (existing.isPresent() && existing.get().getStatus() != Recruiter.Status.PENDING
                && existing.get().getStatus() != Recruiter.Status.FAILED && !request.isForce()) {
            Recruiter r = existing.get();
            return new EmailSendResult("DUPLICATE", null, r.getLastSentAt(), r.getSentCount());
        }

        // 2. Daily quota check
        if (!dailyLimitService.hasQuotaRemaining()) {
            return new EmailSendResult("LIMIT_REACHED", "Daily send limit reached. Try again tomorrow.", null, 0);
        }

        Profile profile = profileService.getProfile();

        String rawSubject = (request.getSubject() != null && !request.getSubject().isBlank())
                ? request.getSubject() : profile.getDefaultSubject();
        String rawBody = (request.getDescription() != null && !request.getDescription().isBlank())
                ? request.getDescription() : profile.getDefaultDescription();

        String subject = emailService.resolvePlaceholders(rawSubject, request.getName());
        String body = emailService.resolvePlaceholders(rawBody, request.getName());

        try {
            emailService.sendResumeEmail(profile, request.getEmail(), request.getName(), subject, body);

            // Upsert the recruiter record so this email is tracked and future
            // duplicate/follow-up logic knows about it.
            Recruiter r = existing.orElseGet(Recruiter::new);
            r.setEmail(request.getEmail());
            if (request.getName() != null && !request.getName().isBlank()) {
                r.setName(request.getName());
            }
            r.setStatus(Recruiter.Status.SENT);
            r.setSentCount(r.getSentCount() + 1);
            r.setLastSentAt(LocalDateTime.now());
            recruiterRepository.save(r);

            return new EmailSendResult("SUCCESS", null, r.getLastSentAt(), r.getSentCount());
        } catch (Exception ex) {
            return new EmailSendResult("FAILURE", ex.getMessage(), null, 0);
        }
    }

    @GetMapping("/logs")
    public List<EmailLog> getLogs() {
        return emailLogRepository.findAll();
    }

    // Lets the frontend show "X / 450 emails sent today" and disable buttons
    // once the quota is exhausted.
    @GetMapping("/quota")
    public QuotaStatus getQuota() {
        return dailyLimitService.getStatus();
    }
}
