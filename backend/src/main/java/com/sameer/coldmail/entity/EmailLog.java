package com.sameer.coldmail.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_log")
@Getter
@Setter
public class EmailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String recruiterName;

    private String recruiterEmail;

    private String subject;

    @Enumerated(EnumType.STRING)
    private Result result;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private LocalDateTime sentAt = LocalDateTime.now();

    public enum Result {
        SUCCESS, FAILURE
    }
}
