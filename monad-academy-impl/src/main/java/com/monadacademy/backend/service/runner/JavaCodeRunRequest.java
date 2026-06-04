package com.monadacademy.backend.service.runner;

import java.util.List;

/**
 * Carries submitted Java source code and validation test cases to the runner.
 *
 * @author Monad Academy Agent
 */
public record JavaCodeRunRequest(
		String sourceCode,
		String methodName,
		List<JavaCodeRunnerTestCase> testCases) {
}
