package com.monadacademy.backend.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Persists coding task content, metadata, and publication state.
 *
 * @author Monad Academy Agent
 */
@Getter
@Entity
@Table(name = "tasks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Task {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 160)
	private String title;

	@Column(nullable = false, unique = true, length = 180)
	private String slug;

	@Column(nullable = false, columnDefinition = "text")
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private TaskDifficulty difficulty;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 64)
	private TaskTopic topic;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private TaskStatus status;

	@Column(nullable = false, columnDefinition = "text")
	private String initialCode;

	@Column(nullable = false, columnDefinition = "text")
	private String solutionTemplate;

	@Column(nullable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	public Task(
			String title,
			String slug,
			String description,
			TaskDifficulty difficulty,
			TaskTopic topic,
			TaskStatus status,
			String initialCode,
			String solutionTemplate) {
		this.title = title;
		this.slug = slug;
		this.description = description;
		this.difficulty = difficulty;
		this.topic = topic;
		this.status = status;
		this.initialCode = initialCode;
		this.solutionTemplate = solutionTemplate;
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

	public void update(
			String title,
			String slug,
			String description,
			TaskDifficulty difficulty,
			TaskTopic topic,
			TaskStatus status,
			String initialCode,
			String solutionTemplate) {
		this.title = title;
		this.slug = slug;
		this.description = description;
		this.difficulty = difficulty;
		this.topic = topic;
		this.status = status;
		this.initialCode = initialCode;
		this.solutionTemplate = solutionTemplate;
		touch();
	}

	public void publish() {
		this.status = TaskStatus.PUBLISHED;
		touch();
	}

	public void archive() {
		this.status = TaskStatus.ARCHIVED;
		touch();
	}

	private void touch() {
		this.updatedAt = Instant.now();
	}
}
