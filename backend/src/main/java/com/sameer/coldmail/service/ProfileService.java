package com.sameer.coldmail.service;

import com.sameer.coldmail.entity.Profile;
import com.sameer.coldmail.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;

    // Always returns the same single profile row, creating it if it doesn't exist yet.
    public Profile getProfile() {
        return profileRepository.findAll().stream().findFirst()
                .orElseGet(() -> profileRepository.save(new Profile()));
    }

    public Profile updateDetails(String fullName, String subject, String description, String senderEmail,
                                  String followUpSubject, String followUpDescription) {
        Profile profile = getProfile();
        profile.setFullName(fullName);
        profile.setDefaultSubject(subject);
        profile.setDefaultDescription(description);
        profile.setSenderEmail(senderEmail);
        profile.setFollowUpSubject(followUpSubject);
        profile.setFollowUpDescription(followUpDescription);
        return profileRepository.save(profile);
    }

    public Profile updateResume(MultipartFile file) throws IOException {
        Profile profile = getProfile();
        profile.setResumeFileName(file.getOriginalFilename());
        profile.setResumeContentType(file.getContentType());
        profile.setResumeData(file.getBytes());
        return profileRepository.save(profile);
    }
}
