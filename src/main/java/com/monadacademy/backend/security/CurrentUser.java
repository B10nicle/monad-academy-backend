package com.monadacademy.backend.security;

import java.util.UUID;

import com.monadacademy.backend.entity.UserRole;

public record CurrentUser(
		UUID id,
		UserRole role) {
}
