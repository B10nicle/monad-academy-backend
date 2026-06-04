package com.monadacademy.backend.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Persists user source code submissions and execution result metadata.
 *
 * @author Monad Academy Agent
 */
@Getter
@Entity
@Table(name = "submissions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Submission {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "task_id", nullable = false)
	private Task task;

	@Column(nullable = false, columnDefinition = "text")
	private String sourceCode;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private SubmissionStatus status;

	@Column(columnDefinition = "text")
	private String executionMetadata;

	private Long executionDurationMs;

	@Column(nullable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	public Submission(User user, Task task, String sourceCode) {
		this.user = user;
		this.task = task;
		this.sourceCode = sourceCode;
		this.status = SubmissionStatus.PENDING;
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

	public void markRunning() {
		this.status = SubmissionStatus.RUNNING;
		touch();
	}

	public void complete(SubmissionStatus status, String executionMetadata, long executionDurationMs) {
		this.status = status;
		this.executionMetadata = executionMetadata;
		this.executionDurationMs = executionDurationMs;
		touch();
	}

	private void touch() {
		this.updatedAt = Instant.now();
	}
}
