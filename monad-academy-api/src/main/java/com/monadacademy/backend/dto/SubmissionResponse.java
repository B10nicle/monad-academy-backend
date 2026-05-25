package com.monadacademy.backend.dto;

import java.time.Instant;
import java.util.UUID;

import com.monadacademy.backend.entity.SubmissionStatus;

/**
 * Represents stored submission data and execution result metadata.
 *
 * @author Monad Academy Agent
 */
public record SubmissionResponse(
		UUID id,
		UUID userId,
		UUID taskId,
		String sourceCode,
		SubmissionStatus status,
		String executionMetadata,
		Long executionDurationMs,
		Instant createdAt,
		Instant updatedAt) {
}
