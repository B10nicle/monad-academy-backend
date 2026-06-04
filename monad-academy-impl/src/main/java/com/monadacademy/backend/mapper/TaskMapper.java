package com.monadacademy.backend.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.monadacademy.backend.dto.PublicTaskResponse;
import com.monadacademy.backend.dto.PublicTaskSummaryResponse;
import com.monadacademy.backend.dto.PublicTaskTestCaseResponse;
import com.monadacademy.backend.dto.TaskResponse;
import com.monadacademy.backend.dto.TaskTestCaseResponse;
import com.monadacademy.backend.entity.Task;
import com.monadacademy.backend.entity.TaskTestCase;

/**
 * Maps task domain entities to API response DTOs.
 *
 * @author Monad Academy Agent
 */
@Mapper(uses = ResponseFormatMapper.class)
public interface TaskMapper {

	@Mapping(target = "testCases", source = "testCases")
	@Mapping(target = "createdAt", source = "task.createdAt", qualifiedByName = "formatInstant")
	@Mapping(target = "updatedAt", source = "task.updatedAt", qualifiedByName = "formatInstant")
	TaskResponse toResponse(Task task, List<TaskTestCase> testCases);

	@Mapping(target = "testCases", source = "testCases")
	@Mapping(target = "createdAt", source = "task.createdAt", qualifiedByName = "formatInstant")
	@Mapping(target = "updatedAt", source = "task.updatedAt", qualifiedByName = "formatInstant")
	PublicTaskResponse toPublicResponse(Task task, List<TaskTestCase> testCases);

	PublicTaskSummaryResponse toPublicSummaryResponse(Task task);

	@Mapping(target = "taskId", source = "task.id")
	@Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "formatInstant")
	TaskTestCaseResponse toResponse(TaskTestCase testCase);

	PublicTaskTestCaseResponse toPublicResponse(TaskTestCase testCase);
}
