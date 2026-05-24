package com.monadacademy.backend.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents validation test case data returned by task APIs.
 *
 * @author Monad Academy Agent
 */
public record TaskTestCaseResponse(
		UUID id,
		UUID taskId,
		String input,
		String expectedOutput,
		boolean hidden,
		int orderIndex,
		Instant createdAt) {
}
