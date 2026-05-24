package com.monadacademy.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.monadacademy.backend.dto.ErrorResponse;

/**
 * Maps application and validation exceptions to API error responses.
 *
 * @author Monad Academy Agent
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(AppException.class)
	ResponseEntity<ErrorResponse> handleAppException(AppException exception) {
		var body = new ErrorResponse(exception.getCode().name(), exception.getMessage());
		return ResponseEntity.status(exception.getStatus()).body(body);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ErrorResponse> handleValidationException() {
		var code = ErrorCode.VALIDATION_ERROR;
		var body = new ErrorResponse(code.name(), code.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}
}
