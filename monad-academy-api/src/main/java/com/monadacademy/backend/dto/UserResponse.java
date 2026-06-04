package com.monadacademy.backend.dto;

import com.monadacademy.backend.entity.UserRole;
import com.monadacademy.backend.entity.UserStatus;

/**
 * Represents public user account data returned by the API.
 *
 * @author Monad Academy Agent
 */
public record UserResponse(
		Long id,
		String email,
		String username,
		UserRole role,
		UserStatus status) {
}
