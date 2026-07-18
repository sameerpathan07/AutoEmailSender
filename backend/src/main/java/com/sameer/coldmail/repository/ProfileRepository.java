package com.sameer.coldmail.repository;

import com.sameer.coldmail.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
}
