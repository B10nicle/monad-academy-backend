package com.monadacademy.backend.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/**
 * Carries application error codes and HTTP statuses through the service layer.
 *
 * @author Monad Academy Agent
 */
@Getter
public class AppException extends RuntimeException {

	private final ErrorCode code;
	private final HttpStatus status;

	public AppException(ErrorCode code, HttpStatus status) {
		super(code.getMessage());
		this.code = code;
		this.status = status;
	}
}
