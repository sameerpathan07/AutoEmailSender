package com.sameer.coldmail.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QuotaStatus {
    private int dailyLimit;
    private int sentToday;
    private int remaining;
}
