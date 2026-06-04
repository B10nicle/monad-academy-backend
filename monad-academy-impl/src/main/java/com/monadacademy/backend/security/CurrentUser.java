package com.monadacademy.backend.security;

import com.monadacademy.backend.entity.UserRole;

/**
 * Represents the authenticated user extracted from a JWT.
 *
 * @author Monad Academy Agent
 */
public record CurrentUser(
		Long id,
		UserRole role) {
}
