package com.sameer.coldmail.controller;

import com.sameer.coldmail.entity.Profile;
import com.sameer.coldmail.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public Profile getProfile() {
        return profileService.getProfile();
    }

    // Update name, subject template, description template, sender email, follow-up templates
    @PutMapping
    public Profile updateDetails(@RequestBody Map<String, String> body) {
        return profileService.updateDetails(
                body.get("fullName"),
                body.get("defaultSubject"),
                body.get("defaultDescription"),
                body.get("senderEmail"),
                body.get("followUpSubject"),
                body.get("followUpDescription")
        );
    }

    // Upload / replace the resume PDF
    @PostMapping("/resume")
    public Profile uploadResume(@RequestParam("file") MultipartFile file) throws IOException {
        return profileService.updateResume(file);
    }
}
