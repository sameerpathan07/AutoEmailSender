package com.sameer.coldmail.controller;

import com.sameer.coldmail.dto.BulkSendResult;
import com.sameer.coldmail.dto.UploadResult;
import com.sameer.coldmail.entity.Recruiter;
import com.sameer.coldmail.service.RecruiterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recruiters")
@RequiredArgsConstructor
public class RecruiterController {

    private final RecruiterService recruiterService;

    @GetMapping
    public List<Recruiter> getAll() {
        return recruiterService.getAll();
    }

    // Bulk upload via Excel file (.xlsx/.xls). Rows whose email already exists
    // are skipped as duplicates - see UploadResult.duplicateEmails.
    @PostMapping("/upload-excel")
    public UploadResult uploadExcel(@RequestParam("file") MultipartFile file) throws IOException {
        return recruiterService.uploadExcel(file);
    }

    // Bulk upload via pasted plain text, one recruiter per line.
    @PostMapping("/upload-text")
    public UploadResult uploadText(@RequestBody Map<String, String> body) {
        return recruiterService.uploadText(body.get("text"));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        recruiterService.delete(id);
    }

    // Sends the resume email to every recruiter still in PENDING status,
    // up to the remaining daily quota. Leftovers stay PENDING for tomorrow.
    @PostMapping("/send-all")
    public BulkSendResult sendAll() {
        return recruiterService.sendToAllPending();
    }

    // Manually trigger a follow-up round (normally runs automatically via
    // DailyEmailScheduler, but exposed here so you can test/run it on demand).
    @PostMapping("/send-followups")
    public BulkSendResult sendFollowUps() {
        return recruiterService.sendFollowUps();
    }
}
