package com.monadacademy.backend.service.runner;

/**
 * Carries one input and expected output pair for Java code validation.
 *
 * @author Monad Academy Agent
 */
public record JavaCodeRunnerTestCase(
		String input,
		String expectedOutput) {
}
