package com.monadacademy.backend.service.runner;

/**
 * Represents an external process execution result.
 *
 * @author Monad Academy Agent
 */
record CommandExecutionResult(
		int exitCode,
		boolean timedOut,
		String output,
		String errorOutput,
		long durationMs) {
}
