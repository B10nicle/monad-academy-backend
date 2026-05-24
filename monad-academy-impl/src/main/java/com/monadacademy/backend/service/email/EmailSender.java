package com.monadacademy.backend.service.email;

/**
 * Defines the abstraction for sending verification emails.
 *
 * @author Monad Academy Agent
 */
public interface EmailSender {

	void sendVerificationEmail(String email, String verificationLink);
}
