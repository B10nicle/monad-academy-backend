package com.monadacademy.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.monadacademy.backend.dto.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Maps application and validation exceptions to API error responses.
 *
 * @author Monad Academy Agent
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(AppException.class)
	ResponseEntity<ErrorResponse> handleAppException(AppException exception) {
		var body = new ErrorResponse(exception.getCode().name(), exception.getMessage());
		log.debug("Handled application exception code={} status={}", exception.getCode(), exception.getStatus());
		return ResponseEntity.status(exception.getStatus()).body(body);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ErrorResponse> handleValidationException() {
		var code = ErrorCode.VALIDATION_ERROR;
		var body = new ErrorResponse(code.name(), code.getMessage());
		log.debug("Handled request validation exception");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}
}
