package com.monadacademy.backend.exception;

import org.springframework.http.HttpStatus;

public class AppException extends RuntimeException {

	private final ErrorCode code;
	private final HttpStatus status;

	public AppException(ErrorCode code, HttpStatus status) {
		super(code.getMessage());
		this.code = code;
		this.status = status;
	}

	public ErrorCode getCode() {
		return code;
	}

	public HttpStatus getStatus() {
		return status;
	}
}
