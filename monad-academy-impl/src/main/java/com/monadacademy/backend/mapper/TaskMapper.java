package com.monadacademy.backend.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.monadacademy.backend.dto.TaskResponse;
import com.monadacademy.backend.dto.TaskTestCaseResponse;
import com.monadacademy.backend.entity.Task;
import com.monadacademy.backend.entity.TaskTestCase;

/**
 * Maps task domain entities to API response DTOs.
 *
 * @author Monad Academy Agent
 */
@Mapper
public interface TaskMapper {

	@Mapping(target = "testCases", source = "testCases")
	TaskResponse toResponse(Task task, List<TaskTestCase> testCases);

	@Mapping(target = "taskId", source = "task.id")
	TaskTestCaseResponse toResponse(TaskTestCase testCase);
}
