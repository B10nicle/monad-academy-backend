package com.monadacademy.backend.exception;

public enum ErrorCode {
	VALIDATION_ERROR("Validation error"),
	EMAIL_ALREADY_EXISTS("Email is already registered"),
	USERNAME_ALREADY_EXISTS("Username is already registered"),
	INVALID_CREDENTIALS("Invalid credentials"),
	EMAIL_NOT_VERIFIED("Email is not verified"),
	USER_BLOCKED("User is blocked"),
	INVALID_VERIFICATION_TOKEN("Invalid verification token"),
	EXPIRED_VERIFICATION_TOKEN("Expired verification token");

	private final String message;

	ErrorCode(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
