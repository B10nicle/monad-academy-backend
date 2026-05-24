package com.monadacademy.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
		@NotBlank
		String login,
		@NotBlank
		String password) {
}
