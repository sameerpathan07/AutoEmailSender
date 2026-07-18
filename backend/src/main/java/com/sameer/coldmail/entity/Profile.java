package com.sameer.coldmail.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

// Single-row table: your profile settings (resume + default email template).
// A "singleton" profile is fine since this app is built for one user (you).
@Entity
@Table(name = "profile")
@Getter
@Setter
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    private String resumeFileName;

    private String resumeContentType;

    @JsonIgnore
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "resume_data")
    private byte[] resumeData;

    @Column(length = 500)
    private String defaultSubject;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String defaultDescription; // supports {{name}} placeholder

    private String senderEmail; // the gmail address you send from

    // Sent automatically ~N days after the first email if the recruiter is still
    // in SENT status. If left blank, a sensible default follow-up is generated
    // from defaultSubject/defaultDescription at send time.
    @Column(length = 500)
    private String followUpSubject;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String followUpDescription; // supports {{name}} placeholder
}
