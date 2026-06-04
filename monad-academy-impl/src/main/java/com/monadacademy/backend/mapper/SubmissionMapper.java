package com.monadacademy.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.monadacademy.backend.dto.SubmissionResponse;
import com.monadacademy.backend.entity.Submission;

/**
 * Maps submission domain entities to API response DTOs.
 *
 * @author Monad Academy Agent
 */
@Mapper(uses = ResponseFormatMapper.class)
public interface SubmissionMapper {

	@Mapping(target = "userId", source = "user.id")
	@Mapping(target = "taskId", source = "task.id")
	@Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "formatInstant")
	@Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "formatInstant")
	@Mapping(target = "executionDurationMs", source = "executionDurationMs", qualifiedByName = "roundDuration")
	SubmissionResponse toResponse(Submission submission);
}
