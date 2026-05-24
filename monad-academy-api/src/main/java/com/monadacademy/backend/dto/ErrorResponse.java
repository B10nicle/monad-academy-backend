package com.monadacademy.backend.dto;

/**
 * Represents the API error response contract.
 *
 * @author Monad Academy Agent
 */
public record ErrorResponse(
		String code,
		String message) {
}
