package com.monadacademy.backend.dto;

/**
 * Represents a simple API message response with a stable localization code.
 *
 * @author Monad Academy Agent
 */
public record MessageResponse(
		MessageCode code,
		String message) {
}
