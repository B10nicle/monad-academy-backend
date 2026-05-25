package com.monadacademy.backend.service.runner;

import com.monadacademy.backend.entity.SubmissionStatus;

/**
 * Represents Java execution outcome and captured process metadata.
 *
 * @author Monad Academy Agent
 */
public record JavaCodeRunResult(
		SubmissionStatus status,
		String output,
		String errorOutput,
		String executionMetadata,
		long executionDurationMs) {
}
