package com.sameer.coldmail.service;

import com.sameer.coldmail.dto.QuotaStatus;
import com.sameer.coldmail.entity.EmailLog;
import com.sameer.coldmail.repository.EmailLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Gmail caps free accounts at ~500 emails/day. We stay under that with a
// configurable daily limit (default 450) shared between fresh sends and
// follow-ups, so a big bulk send automatically continues the next day
// instead of getting your account flagged or blocked.
@Service
@RequiredArgsConstructor
public class DailyLimitService {

    private final EmailLogRepository emailLogRepository;

    @Value("${coldmail.daily-limit:450}")
    private int dailyLimit;

    public int getSentToday() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return (int) emailLogRepository.countByResultAndSentAtBetween(
                EmailLog.Result.SUCCESS, startOfDay, endOfDay);
    }

    public int getRemainingQuota() {
        return Math.max(0, dailyLimit - getSentToday());
    }

    public boolean hasQuotaRemaining() {
        return getRemainingQuota() > 0;
    }

    public QuotaStatus getStatus() {
        int sentToday = getSentToday();
        return new QuotaStatus(dailyLimit, sentToday, Math.max(0, dailyLimit - sentToday));
    }

    public int getDailyLimit() {
        return dailyLimit;
    }
}
