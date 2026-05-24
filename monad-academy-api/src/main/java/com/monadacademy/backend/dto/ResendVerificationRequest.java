package com.monadacademy.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Represents a verification email resend request.
 *
 * @author Monad Academy Agent
 */
public record ResendVerificationRequest(
		@Email
		@NotBlank
		String email) {
}
