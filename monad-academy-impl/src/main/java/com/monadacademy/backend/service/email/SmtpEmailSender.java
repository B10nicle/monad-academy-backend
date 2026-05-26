package com.monadacademy.backend.service.email;

import org.springframework.mail.MailException;
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
@RequiredArgsConstructor
public class SmtpEmailSender implements EmailSender {

	private static final String VERIFICATION_EMAIL_SUBJECT = "Verify your Monad Academy account";
	private static final String VERIFICATION_EMAIL_TEMPLATE = """
			<p>Please click the following link to verify your Monad Academy account:</p>
			<p><a href="%s">Verify email</a></p>
			""";

	private final JavaMailSender mailSender;

	@Override
	public void sendVerificationEmail(String email, String verificationLink) {
		log.debug("Sending verification email to {}", email);
		try {
			var message = mailSender.createMimeMessage();
			var helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setTo(email);
			helper.setSubject(VERIFICATION_EMAIL_SUBJECT);
			helper.setText(VERIFICATION_EMAIL_TEMPLATE.formatted(verificationLink), true);

			mailSender.send(message);
			log.debug("Verification email sent successfully to {}", email);
		}
		catch (MailException | MessagingException exception) {
			log.error("Failed to send verification email to {}", email, exception);
			throw new IllegalStateException("Email delivery failed", exception);
		}
	}
}
