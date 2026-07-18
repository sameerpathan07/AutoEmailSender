package com.sameer.coldmail.repository;

import com.sameer.coldmail.entity.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {

    // Used to enforce the daily sending cap - counts successful sends since midnight.
    long countByResultAndSentAtBetween(EmailLog.Result result, LocalDateTime start, LocalDateTime end);
}
