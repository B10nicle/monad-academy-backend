package com.monadacademy.backend.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.monadacademy.backend.entity.TaskDifficulty;
import com.monadacademy.backend.entity.TaskStatus;
import com.monadacademy.backend.entity.TaskTopic;

/**
 * Represents task data returned by task APIs.
 *
 * @author Monad Academy Agent
 */
public record TaskResponse(
		UUID id,
		String title,
		String slug,
		String description,
		TaskDifficulty difficulty,
		TaskTopic topic,
		TaskStatus status,
		String initialCode,
		String solutionTemplate,
		Instant createdAt,
		Instant updatedAt,
		List<TaskTestCaseResponse> testCases) {
}
