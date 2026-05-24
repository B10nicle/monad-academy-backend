package com.monadacademy.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Represents a user registration request.
 *
 * @author Monad Academy Agent
 */
public record RegisterRequest(
		@Email
		@NotBlank
		String email,
		@NotBlank
		String username,
		@NotBlank
		@Size(min = 8)
		String password) {
}
