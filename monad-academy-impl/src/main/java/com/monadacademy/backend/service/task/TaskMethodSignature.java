package com.monadacademy.backend.service.task;

/**
 * Carries a Java solution method signature configured for a task.
 *
 * @author Monad Academy Agent
 */
public record TaskMethodSignature(
		String methodName,
		String methodReturnType,
		String methodParameters) {
}
