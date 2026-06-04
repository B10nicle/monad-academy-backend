package com.monadacademy.backend.dto;

import com.monadacademy.backend.entity.SubmissionStatus;

/**
 * Represents stored submission data and execution result metadata.
 *
 * @author Monad Academy Agent
 */
public record SubmissionResponse(
		Long id,
		Long userId,
		Long taskId,
		String sourceCode,
		SubmissionStatus status,
		String executionMetadata,
		Long executionDurationMs,
		String createdAt,
		String updatedAt) {
}
