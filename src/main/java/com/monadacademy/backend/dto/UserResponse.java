package com.monadacademy.backend.dto;

import java.util.UUID;

import com.monadacademy.backend.entity.UserRole;
import com.monadacademy.backend.entity.UserStatus;

public record UserResponse(
		UUID id,
		String email,
		String username,
		UserRole role,
		UserStatus status) {
}
