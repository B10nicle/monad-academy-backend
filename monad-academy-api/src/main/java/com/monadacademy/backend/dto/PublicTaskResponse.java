package com.monadacademy.backend.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.monadacademy.backend.entity.TaskDifficulty;
import com.monadacademy.backend.entity.TaskTopic;

/**
 * Represents published task details available to public clients.
 *
 * @author Monad Academy Agent
 */
public record PublicTaskResponse(
		UUID id,
		String title,
		String slug,
		String description,
		TaskDifficulty difficulty,
		TaskTopic topic,
		String initialCode,
		Instant createdAt,
		Instant updatedAt,
		List<PublicTaskTestCaseResponse> testCases) {
}
