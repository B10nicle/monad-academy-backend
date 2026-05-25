package com.monadacademy.backend.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.monadacademy.backend.dto.PageResponse;
import com.monadacademy.backend.dto.SubmissionResponse;
import com.monadacademy.backend.entity.SubmissionStatus;
import com.monadacademy.backend.service.submission.SubmissionService;

import lombok.RequiredArgsConstructor;

/**
 * Exposes admin endpoints for reviewing user submissions.
 *
 * @author Monad Academy Agent
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminSubmissionController {

	private final SubmissionService submissionService;

	@GetMapping("/submissions")
	PageResponse<SubmissionResponse> listSubmissions(
			@RequestParam(required = false) UUID userId,
			@RequestParam(required = false) UUID taskId,
			@RequestParam(required = false) SubmissionStatus status,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return submissionService.listAdminSubmissions(userId, taskId, status, page, size);
	}

	@GetMapping("/users/{userId}/submissions")
	PageResponse<SubmissionResponse> listUserSubmissions(
			@PathVariable UUID userId,
			@RequestParam(required = false) UUID taskId,
			@RequestParam(required = false) SubmissionStatus status,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return submissionService.listAdminUserSubmissions(userId, taskId, status, page, size);
	}
}
