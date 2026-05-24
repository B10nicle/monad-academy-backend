package com.monadacademy.backend.dto;

/**
 * Represents a successful authentication response with a JWT token.
 *
 * @author Monad Academy Agent
 */
public record AuthResponse(String token) {
}
