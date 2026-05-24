package com.monadacademy.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Represents validation test case input submitted through task APIs.
 *
 * @author Monad Academy Agent
 */
public record TaskTestCaseRequest(
		@NotBlank
		String input,
		@NotBlank
		String expectedOutput,
		boolean hidden,
		@Min(0)
		int orderIndex) {
}
