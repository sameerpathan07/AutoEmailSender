package com.sameer.coldmail.dto;

import com.sameer.coldmail.entity.Recruiter;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

// Returned after a bulk Excel/text upload so the frontend can clearly show
// how many were newly added vs. skipped as duplicates.
@Getter
@AllArgsConstructor
public class UploadResult {
    private int addedCount;
    private int duplicateCount;
    private List<Recruiter> added;
    private List<String> duplicateEmails;
}
