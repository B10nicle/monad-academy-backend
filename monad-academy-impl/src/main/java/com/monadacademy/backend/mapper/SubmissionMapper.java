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
@Mapper
public interface SubmissionMapper {

	@Mapping(target = "userId", source = "user.id")
	@Mapping(target = "taskId", source = "task.id")
	SubmissionResponse toResponse(Submission submission);
}
