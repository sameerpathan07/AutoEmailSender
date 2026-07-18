package com.sameer.coldmail.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "recruiter")
@Getter
@Setter
public class Recruiter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // nullable - some rows may have no name

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime lastSentAt;

    // 0 = never contacted, 1 = initial email sent, 2 = follow-up sent.
    // Capped at 1 follow-up so we don't keep pestering the same HR forever.
    private int sentCount = 0;

    public enum Status {
        PENDING, SENT, FOLLOWED_UP, FAILED
    }
}
