package com.monadacademy.backend.dto;

import java.util.List;

import com.monadacademy.backend.entity.TaskDifficulty;
import com.monadacademy.backend.entity.TaskStatus;
import com.monadacademy.backend.entity.TaskTopic;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Represents task content and metadata submitted through task APIs.
 *
 * @author Monad Academy Agent
 */
public record TaskRequest(
		@NotBlank
		@Size(max = 160)
		String title,
		@NotBlank
		@Size(max = 180)
		@Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$")
		String slug,
		@NotBlank
		String description,
		@NotNull
		TaskDifficulty difficulty,
		@NotNull
		TaskTopic topic,
		@NotNull
		TaskStatus status,
		@NotBlank
		String initialCode,
		@NotBlank
		String solutionTemplate,
		@Valid
		List<TaskTestCaseRequest> testCases) {
}
