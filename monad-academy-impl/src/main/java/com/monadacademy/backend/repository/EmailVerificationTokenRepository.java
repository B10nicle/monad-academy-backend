package com.monadacademy.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.monadacademy.backend.entity.EmailVerificationToken;

/**
 * Provides persistence operations for email verification tokens.
 *
 * @author Monad Academy Agent
 */
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

	Optional<EmailVerificationToken> findByTokenHash(String tokenHash);
}
