package com.sameer.coldmail.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class BulkSendResult {
    private int totalAttempted;
    private int successCount;
    private int failureCount;
    private List<String> failedEmails;

    // How many were left un-sent because the daily quota ran out (they stay
    // queued and will go out automatically once quota resets).
    private int remainingInQueue;

    // True if this batch stopped specifically because the daily limit was hit.
    private boolean limitReached;
}
