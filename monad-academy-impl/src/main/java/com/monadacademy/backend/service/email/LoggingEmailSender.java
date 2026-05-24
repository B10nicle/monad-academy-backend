package com.monadacademy.backend.service.email;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Logs verification links for local development email delivery.
 *
 * @author Monad Academy Agent
 */
@Slf4j
@Service
public class LoggingEmailSender implements EmailSender {

	@Override
	public void sendVerificationEmail(String email, String verificationLink) {
		log.info("Verification email for {}: {}", email, verificationLink);
	}
}
