package com.monadacademy.backend.entity;

/**
 * Defines supported audit log event types.
 *
 * @author Monad Academy Agent
 */
public enum AuditEventType {
	USER_REGISTERED,
	USER_EMAIL_VERIFIED,
	USER_VERIFICATION_EMAIL_RESENT,
	USER_LOGIN_SUCCEEDED,
	USER_LOGIN_FAILED
}
