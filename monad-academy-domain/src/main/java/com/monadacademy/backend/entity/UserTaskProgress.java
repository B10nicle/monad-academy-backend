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
import jakarta.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Persists a user's solving progress for one coding task.
 *
 * @author Monad Academy Agent
 */
@Getter
@Entity
@Table(
		name = "user_task_progress",
		uniqueConstraints = @UniqueConstraint(name = "uk_user_task_progress_user_task", columnNames = {"user_id", "task_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTaskProgress {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "task_id", nullable = false)
	private Task task;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private UserTaskProgressStatus status;

	@Column(nullable = false)
	private int attemptsCount;

	private Instant solvedAt;

	@Column(nullable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	public UserTaskProgress(User user, Task task) {
		this.user = user;
		this.task = task;
		this.status = UserTaskProgressStatus.IN_PROGRESS;
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

	public void recordAttempt(SubmissionStatus submissionStatus) {
		this.attemptsCount++;
		if (submissionStatus == SubmissionStatus.ACCEPTED) {
			this.status = UserTaskProgressStatus.SOLVED;
			if (this.solvedAt == null) {
				this.solvedAt = Instant.now();
			}
		}
		touch();
	}

	private void touch() {
		this.updatedAt = Instant.now();
	}
}
