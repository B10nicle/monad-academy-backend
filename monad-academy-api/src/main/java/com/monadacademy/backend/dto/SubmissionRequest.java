package com.monadacademy.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Represents source code submitted by an authenticated user for a task.
 *
 * @author Monad Academy Agent
 */
public record SubmissionRequest(
		@NotNull
		Long taskId,
		@NotBlank
		String sourceCode) {
}
