package com.monadacademy.backend.service.email;

import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Sends verification emails using SMTP.
 *
 * @author Monad Academy Agent
 */
@Slf4j
@Service
@Profile("prod")
@RequiredArgsConstructor
public class SmtpEmailSender implements EmailSender {

	private final JavaMailSender mailSender;

	@Override
	public void sendVerificationEmail(String email, String verificationLink) {
		log.debug("Sending verification email to {}", email);
		try {
			var message = mailSender.createMimeMessage();
			var helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setTo(email);
			helper.setSubject("Verify your Monad Academy account");
			helper.setText("Please click the following link to verify your account: " + verificationLink, true);

			mailSender.send(message);
			log.debug("Verification email sent successfully to {}", email);
		} catch (MessagingException e) {
			log.error("Failed to send verification email to {}: {}", email, e.getMessage());
			throw new RuntimeException("Email delivery failed", e);
		}
	}
}
