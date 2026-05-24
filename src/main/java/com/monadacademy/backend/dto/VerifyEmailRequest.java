package com.monadacademy.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(
		@NotBlank
		String token) {
}
