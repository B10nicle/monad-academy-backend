package com.monadacademy.backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.monadacademy.backend.entity.EmailVerificationToken;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {

	Optional<EmailVerificationToken> findByTokenHash(String tokenHash);
}
