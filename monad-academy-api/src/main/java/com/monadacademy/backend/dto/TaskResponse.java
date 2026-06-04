package com.monadacademy.backend.dto;

import java.time.Instant;
import java.util.List;

import com.monadacademy.backend.entity.TaskDifficulty;
import com.monadacademy.backend.entity.TaskStatus;
import com.monadacademy.backend.entity.TaskTopic;

/**
 * Represents task data returned by task APIs.
 *
 * @author Monad Academy Agent
 */
public record TaskResponse(
		Long id,
		String title,
		String slug,
		String description,
		String methodName,
		String methodReturnType,
		String methodParameters,
		TaskDifficulty difficulty,
		TaskTopic topic,
		TaskStatus status,
		String initialCode,
		String solutionTemplate,
		Instant createdAt,
		Instant updatedAt,
		List<TaskTestCaseResponse> testCases) {
}
