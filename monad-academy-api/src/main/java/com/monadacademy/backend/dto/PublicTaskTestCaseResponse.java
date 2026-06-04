package com.monadacademy.backend.dto;

/**
 * Represents public sample test case data for a published task.
 *
 * @author Monad Academy Agent
 */
public record PublicTaskTestCaseResponse(
		Long id,
		String input,
		String expectedOutput,
		int orderIndex) {
}
