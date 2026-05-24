package com.monadacademy.backend.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.monadacademy.backend.dto.TaskResponse;
import com.monadacademy.backend.dto.TaskTestCaseResponse;
import com.monadacademy.backend.entity.Task;
import com.monadacademy.backend.entity.TaskTestCase;

/**
 * Maps task domain entities to API response DTOs.
 *
 * @author Monad Academy Agent
 */
@Component
public class TaskMapper {

	public TaskResponse toResponse(Task task, List<TaskTestCase> testCases) {
		return new TaskResponse(
				task.getId(),
				task.getTitle(),
				task.getSlug(),
				task.getDescription(),
				task.getDifficulty(),
				task.getTopic(),
				task.getStatus(),
				task.getInitialCode(),
				task.getSolutionTemplate(),
				task.getCreatedAt(),
				task.getUpdatedAt(),
				testCases.stream()
						.map(this::toResponse)
						.toList());
	}

	public TaskTestCaseResponse toResponse(TaskTestCase testCase) {
		return new TaskTestCaseResponse(
				testCase.getId(),
				testCase.getTask().getId(),
				testCase.getInput(),
				testCase.getExpectedOutput(),
				testCase.isHidden(),
				testCase.getOrderIndex(),
				testCase.getCreatedAt());
	}
}
