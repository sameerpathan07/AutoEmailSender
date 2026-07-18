package com.sameer.coldmail.service;

import com.sameer.coldmail.dto.BulkSendResult;
import com.sameer.coldmail.dto.UploadResult;
import com.sameer.coldmail.entity.Profile;
import com.sameer.coldmail.entity.Recruiter;
import com.sameer.coldmail.repository.RecruiterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecruiterService {

    private final RecruiterRepository recruiterRepository;
    private final ExcelParserService excelParserService;
    private final EmailService emailService;
    private final ProfileService profileService;
    private final DailyLimitService dailyLimitService;

    @Value("${coldmail.followup-after-days:4}")
    private int followUpAfterDays;

    public UploadResult uploadExcel(MultipartFile file) throws IOException {
        return saveNewOnes(excelParserService.parseExcel(file));
    }

    public UploadResult uploadText(String text) {
        return saveNewOnes(excelParserService.parseText(text));
    }

    // Any email already in the recruiter table (case-insensitive) is treated as
    // a duplicate and skipped - it is NOT re-added or re-queued. The caller gets
    // back exactly which emails were skipped so the frontend can show a clear message.
    private UploadResult saveNewOnes(List<Recruiter> parsed) {
        List<Recruiter> added = new ArrayList<>();
        List<String> duplicates = new ArrayList<>();

        for (Recruiter r : parsed) {
            boolean alreadyExists = recruiterRepository.findByEmailIgnoreCase(r.getEmail()).isPresent();
            if (alreadyExists) {
                duplicates.add(r.getEmail());
            } else {
                added.add(recruiterRepository.save(r));
            }
        }

        return new UploadResult(added.size(), duplicates.size(), added, duplicates);
    }

    public List<Recruiter> getAll() {
        return recruiterRepository.findAll();
    }

    public void delete(Long id) {
        recruiterRepository.deleteById(id);
    }

    // Sends the resume email to PENDING recruiters, stopping as soon as the
    // shared daily quota (default 450) runs out. Anything left over simply
    // stays PENDING and is picked up automatically the next day.
    public BulkSendResult sendToAllPending() {
        Profile profile = profileService.getProfile();
        List<Recruiter> pending = recruiterRepository.findByStatus(Recruiter.Status.PENDING);

        int remaining = dailyLimitService.getRemainingQuota();
        int toSend = Math.min(pending.size(), remaining);

        int success = 0;
        List<String> failed = new ArrayList<>();

        for (int i = 0; i < toSend; i++) {
            Recruiter r = pending.get(i);
            try {
                String subject = emailService.resolvePlaceholders(profile.getDefaultSubject(), r.getName());
                String body = emailService.resolvePlaceholders(profile.getDefaultDescription(), r.getName());
                emailService.sendResumeEmail(profile, r.getEmail(), r.getName(), subject, body);
                r.setStatus(Recruiter.Status.SENT);
                r.setSentCount(1);
                r.setLastSentAt(LocalDateTime.now());
                success++;
            } catch (Exception ex) {
                r.setStatus(Recruiter.Status.FAILED);
                failed.add(r.getEmail());
            }
            recruiterRepository.save(r);
        }

        int stillPending = pending.size() - toSend;
        return new BulkSendResult(toSend, success, failed.size(), failed, stillPending, remaining == 0);
    }

    // Sends a single follow-up round: recruiters who were emailed exactly once
    // and it's been >= followUpAfterDays since then. Runs on the scheduler
    // (see DailyEmailScheduler) but can also be triggered manually.
    // NOTE: this cannot detect actual replies (no inbox/IMAP integration) -
    // it follows up unconditionally after the configured number of days.
    public BulkSendResult sendFollowUps() {
        Profile profile = profileService.getProfile();
        LocalDateTime cutoff = LocalDateTime.now().minusDays(followUpAfterDays);

        List<Recruiter> candidates = recruiterRepository
                .findByStatusAndSentCountAndLastSentAtBefore(Recruiter.Status.SENT, 1, cutoff);

        int remaining = dailyLimitService.getRemainingQuota();
        int toSend = Math.min(candidates.size(), remaining);

        int success = 0;
        List<String> failed = new ArrayList<>();

        String followUpSubjectTemplate = (profile.getFollowUpSubject() != null && !profile.getFollowUpSubject().isBlank())
                ? profile.getFollowUpSubject()
                : "Following up: " + (profile.getDefaultSubject() != null ? profile.getDefaultSubject() : "my application");

        String followUpBodyTemplate = (profile.getFollowUpDescription() != null && !profile.getFollowUpDescription().isBlank())
                ? profile.getFollowUpDescription()
                : "Hi {{name}},\n\nJust following up on my previous email regarding my application - "
                        + "I understand things get busy, so wanted to bump this to the top of your inbox. "
                        + "My resume is attached again for convenience.\n\nThanks for your time!";

        for (int i = 0; i < toSend; i++) {
            Recruiter r = candidates.get(i);
            try {
                String subject = emailService.resolvePlaceholders(followUpSubjectTemplate, r.getName());
                String body = emailService.resolvePlaceholders(followUpBodyTemplate, r.getName());
                emailService.sendResumeEmail(profile, r.getEmail(), r.getName(), subject, body);
                r.setStatus(Recruiter.Status.FOLLOWED_UP);
                r.setSentCount(2);
                r.setLastSentAt(LocalDateTime.now());
                success++;
            } catch (Exception ex) {
                failed.add(r.getEmail());
            }
            recruiterRepository.save(r);
        }

        int stillWaiting = candidates.size() - toSend;
        return new BulkSendResult(toSend, success, failed.size(), failed, stillWaiting, remaining == 0);
    }
}
