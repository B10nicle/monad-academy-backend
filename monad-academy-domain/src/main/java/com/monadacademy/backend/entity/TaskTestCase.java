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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Persists executable validation data for a coding task.
 *
 * @author Monad Academy Agent
 */
@Getter
@Entity
@Table(name = "task_test_cases")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TaskTestCase {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "task_id", nullable = false)
	private Task task;

	@Column(nullable = false, columnDefinition = "text")
	private String input;

	@Column(nullable = false, columnDefinition = "text")
	private String expectedOutput;

	@Column(nullable = false)
	private boolean hidden;

	@Column(nullable = false)
	private int orderIndex;

	@Column(nullable = false)
	private Instant createdAt;

	public TaskTestCase(Task task, String input, String expectedOutput, boolean hidden, int orderIndex) {
		this.id = UUID.randomUUID();
		this.task = task;
		this.input = input;
		this.expectedOutput = expectedOutput;
		this.hidden = hidden;
		this.orderIndex = orderIndex;
	}

	@PrePersist
	void prePersist() {
		this.createdAt = Instant.now();
	}
}
