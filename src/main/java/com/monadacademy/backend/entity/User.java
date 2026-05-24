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

@Entity
@Table(name = "users")
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

	protected User() {
	}

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

	public UUID getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getUsername() {
		return username;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public UserRole getRole() {
		return role;
	}

	public UserStatus getStatus() {
		return status;
	}

	public Instant getEmailVerifiedAt() {
		return emailVerifiedAt;
	}

	public Instant getLastLoginAt() {
		return lastLoginAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
