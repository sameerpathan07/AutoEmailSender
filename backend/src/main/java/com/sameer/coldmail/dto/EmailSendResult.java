package com.sameer.coldmail.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// Response for the single-send endpoint. status can be:
// SUCCESS, FAILURE, DUPLICATE (already contacted - needs "force" to resend), LIMIT_REACHED
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmailSendResult {
    private String status;
    private String error;
    private LocalDateTime lastSentAt;
    private int sentCountSoFar;
}
