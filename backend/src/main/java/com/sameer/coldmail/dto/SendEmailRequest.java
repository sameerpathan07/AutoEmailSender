package com.sameer.coldmail.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

// Used by the "single send" form on the Home page.
// name is optional - HR name may not always be known.
@Getter
@Setter
public class SendEmailRequest {

    private String name;

    @NotBlank
    @Email
    private String email;

    // Optional overrides - if blank, the profile's default subject/description are used
    private String subject;
    private String description;

    // If a recruiter with this email was already contacted, sendSingle() will
    // block by default and return status=DUPLICATE. Set force=true (after the
    // user confirms on the frontend) to send anyway.
    private boolean force = false;
}
