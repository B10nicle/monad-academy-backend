package com.monadacademy.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Represents a login request by email or username.
 *
 * @author Monad Academy Agent
 */
public record LoginRequest(
		@NotBlank
		String login,
		@NotBlank
		String password) {
}
