package com.monadacademy.backend.dto;

/**
 * Represents validation test case data returned by task APIs.
 *
 * @author Monad Academy Agent
 */
public record TaskTestCaseResponse(
		Long id,
		Long taskId,
		String input,
		String expectedOutput,
		boolean hidden,
		int orderIndex,
		String createdAt) {
}
