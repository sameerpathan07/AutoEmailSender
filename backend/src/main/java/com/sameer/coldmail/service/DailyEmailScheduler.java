package com.sameer.coldmail.service;

import com.sameer.coldmail.dto.BulkSendResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// Runs once a day and automatically sends follow-up emails to anyone who
// was emailed exactly once and hasn't been contacted again in
// coldmail.followup-after-days days. Shares the same daily quota as
// everything else, so it will never push you over the Gmail limit.
//
// Default: runs at 10:00 AM server time every day.
// Change the cron below to adjust timing (fields are: sec min hour day month weekday).
@Component
@RequiredArgsConstructor
@Slf4j
public class DailyEmailScheduler {

    private final RecruiterService recruiterService;

    @Scheduled(cron = "${coldmail.followup-cron:0 0 10 * * *}")
    public void runDailyFollowUps() {
        log.info("Running scheduled follow-up email job...");
        BulkSendResult result = recruiterService.sendFollowUps();
        log.info("Follow-up job done: {} sent, {} failed, {} still waiting (limitReached={})",
                result.getSuccessCount(), result.getFailureCount(),
                result.getRemainingInQueue(), result.isLimitReached());
    }
}
