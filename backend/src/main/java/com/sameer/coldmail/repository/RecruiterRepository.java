package com.sameer.coldmail.repository;

import com.sameer.coldmail.entity.Recruiter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RecruiterRepository extends JpaRepository<Recruiter, Long> {
    List<Recruiter> findByStatus(Recruiter.Status status);
    Optional<Recruiter> findByEmailIgnoreCase(String email);

    // Follow-up candidates: emailed exactly once, still no second contact,
    // and it's been long enough since the first email.
    List<Recruiter> findByStatusAndSentCountAndLastSentAtBefore(
            Recruiter.Status status, int sentCount, LocalDateTime cutoff);
}
