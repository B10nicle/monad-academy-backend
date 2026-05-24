package com.monadacademy.backend.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "email_verification_tokens")
public class EmailVerificationToken {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false, unique = true, length = 128)
	private String tokenHash;

	@Column(nullable = false)
	private Instant expiresAt;

	private Instant confirmedAt;

	@Column(nullable = false)
	private Instant createdAt;

	protected EmailVerificationToken() {
	}

	public EmailVerificationToken(User user, String tokenHash, Instant expiresAt) {
		this.id = UUID.randomUUID();
		this.user = user;
		this.tokenHash = tokenHash;
		this.expiresAt = expiresAt;
	}

	@PrePersist
	void prePersist() {
		this.createdAt = Instant.now();
	}

	public void confirm() {
		this.confirmedAt = Instant.now();
	}

	public boolean isConfirmed() {
		return confirmedAt != null;
	}

	public boolean isExpired() {
		return expiresAt.isBefore(Instant.now());
	}

	public UUID getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public String getTokenHash() {
		return tokenHash;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public Instant getConfirmedAt() {
		return confirmedAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
