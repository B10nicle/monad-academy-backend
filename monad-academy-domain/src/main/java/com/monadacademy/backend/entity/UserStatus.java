package com.monadacademy.backend.entity;

/**
 * Defines lifecycle states for user accounts.
 *
 * @author Monad Academy Agent
 */
public enum UserStatus {
	PENDING_EMAIL_VERIFICATION,
	ACTIVE,
	BLOCKED,
	DELETED
}
