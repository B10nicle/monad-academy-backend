package com.monadacademy.backend.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Persists user identity, role, status, and authentication metadata.
 *
 * @author Monad Academy Agent
 */
@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

	@Id
	private UUID id;

	@Column(nullable = false, unique = true, length = 320)
	private String email;

	@Column(nullable = false, unique = true, length = 64)
	private String username;

	@Column(nullable = false)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private UserRole role;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 64)
	private UserStatus status;

	private Instant emailVerifiedAt;

	private Instant lastLoginAt;

	@Column(nullable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	public User(String email, String username, String passwordHash) {
		this.id = UUID.randomUUID();
		this.email = email;
		this.username = username;
		this.passwordHash = passwordHash;
		this.role = UserRole.USER;
		this.status = UserStatus.PENDING_EMAIL_VERIFICATION;
	}

	@PrePersist
	void prePersist() {
		var now = Instant.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void preUpdate() {
		this.updatedAt = Instant.now();
	}

	public void activate() {
		this.status = UserStatus.ACTIVE;
		this.emailVerifiedAt = Instant.now();
	}

	public void updateLastLoginAt() {
		this.lastLoginAt = Instant.now();
	}
}
