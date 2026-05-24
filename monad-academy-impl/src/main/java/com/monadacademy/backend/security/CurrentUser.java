package com.monadacademy.backend.security;

import java.util.UUID;

import com.monadacademy.backend.entity.UserRole;

/**
 * Represents the authenticated user extracted from a JWT.
 *
 * @author Monad Academy Agent
 */
public record CurrentUser(
		UUID id,
		UserRole role) {
}
