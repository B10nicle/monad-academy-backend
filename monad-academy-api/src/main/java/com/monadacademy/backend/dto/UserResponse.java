package com.monadacademy.backend.dto;

import java.util.UUID;

import com.monadacademy.backend.entity.UserRole;
import com.monadacademy.backend.entity.UserStatus;

/**
 * Represents public user account data returned by the API.
 *
 * @author Monad Academy Agent
 */
public record UserResponse(
		UUID id,
		String email,
		String username,
		UserRole role,
		UserStatus status) {
}
