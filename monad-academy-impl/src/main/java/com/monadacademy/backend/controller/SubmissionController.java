package com.monadacademy.backend.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.monadacademy.backend.dto.PageResponse;
import com.monadacademy.backend.dto.SubmissionRequest;
import com.monadacademy.backend.dto.SubmissionResponse;
import com.monadacademy.backend.service.submission.SubmissionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Exposes authenticated submission execution and history endpoints.
 *
 * @author Monad Academy Agent
 */
@RestController
@RequiredArgsConstructor
public class SubmissionController {

	private final SubmissionService submissionService;

	@PostMapping("/api/submissions")
	SubmissionResponse createSubmission(@Valid @RequestBody SubmissionRequest request) {
		return submissionService.createSubmission(request);
	}

	@GetMapping("/api/submissions/my")
	PageResponse<SubmissionResponse> listCurrentUserSubmissions(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return submissionService.listCurrentUserSubmissions(page, size);
	}

	@GetMapping("/api/tasks/{taskId}/submissions/my")
	PageResponse<SubmissionResponse> listCurrentUserTaskSubmissions(
			@PathVariable UUID taskId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return submissionService.listCurrentUserTaskSubmissions(taskId, page, size);
	}
}
