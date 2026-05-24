package com.monadacademy.backend.exception;

import lombok.Getter;

/**
 * Defines stable API error codes and default messages.
 *
 * @author Monad Academy Agent
 */
@Getter
public enum ErrorCode {
	VALIDATION_ERROR("Validation error"),
	EMAIL_ALREADY_EXISTS("Email is already registered"),
	USERNAME_ALREADY_EXISTS("Username is already registered"),
	INVALID_CREDENTIALS("Invalid credentials"),
	EMAIL_NOT_VERIFIED("Email is not verified"),
	USER_BLOCKED("User is blocked"),
	INVALID_VERIFICATION_TOKEN("Invalid verification token"),
	EXPIRED_VERIFICATION_TOKEN("Expired verification token"),
	TASK_NOT_FOUND("Task not found"),
	TASK_SLUG_ALREADY_EXISTS("Task slug is already used");

	private final String message;

	ErrorCode(String message) {
		this.message = message;
	}
}
