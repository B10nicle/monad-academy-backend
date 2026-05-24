package com.monadacademy.backend.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Persists security-relevant user activity events.
 *
 * @author Monad Academy Agent
 */
@Getter
@Entity
@Table(name = "audit_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditLog {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 64)
	private AuditEventType eventType;

	@Column(columnDefinition = "text")
	private String metadata;

	@Column(nullable = false)
	private Instant createdAt;

	public AuditLog(User user, AuditEventType eventType, String metadata) {
		this.id = UUID.randomUUID();
		this.user = user;
		this.eventType = eventType;
		this.metadata = metadata;
	}

	@PrePersist
	void prePersist() {
		this.createdAt = Instant.now();
	}
}
