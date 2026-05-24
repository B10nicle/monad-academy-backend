package com.monadacademy.backend.service.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Logs verification links for local development email delivery.
 *
 * @author Monad Academy Agent
 */
@Service
public class LoggingEmailSender implements EmailSender {

	private static final Logger log = LoggerFactory.getLogger(LoggingEmailSender.class);

	@Override
	public void sendVerificationEmail(String email, String verificationLink) {
		log.info("Verification email for {}: {}", email, verificationLink);
	}
}
