package com.monadacademy.backend.dto;

import java.util.UUID;

/**
 * Represents public sample test case data for a published task.
 *
 * @author Monad Academy Agent
 */
public record PublicTaskTestCaseResponse(
		UUID id,
		String input,
		String expectedOutput,
		int orderIndex) {
}
