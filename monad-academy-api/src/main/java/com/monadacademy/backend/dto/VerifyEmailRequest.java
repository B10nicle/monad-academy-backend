package com.monadacademy.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Represents an email verification request.
 *
 * @author Monad Academy Agent
 */
public record VerifyEmailRequest(
		@NotBlank
		String token) {
}
