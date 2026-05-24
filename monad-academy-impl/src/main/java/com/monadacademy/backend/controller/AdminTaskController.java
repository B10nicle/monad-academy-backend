package com.monadacademy.backend.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.monadacademy.backend.dto.TaskRequest;
import com.monadacademy.backend.dto.TaskResponse;
import com.monadacademy.backend.dto.TaskTestCaseRequest;
import com.monadacademy.backend.dto.TaskTestCaseResponse;
import com.monadacademy.backend.service.task.AdminTaskService;

import lombok.RequiredArgsConstructor;

/**
 * Exposes admin endpoints for task management and lifecycle changes.
 *
 * @author Monad Academy Agent
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/tasks")
public class AdminTaskController {

	private final AdminTaskService adminTaskService;

	@PostMapping
	TaskResponse createTask(@Valid @RequestBody TaskRequest request) {
		return adminTaskService.createTask(request);
	}

	@PutMapping("/{id}")
	TaskResponse updateTask(@PathVariable UUID id, @Valid @RequestBody TaskRequest request) {
		return adminTaskService.updateTask(id, request);
	}

	@PostMapping("/{id}/publish")
	TaskResponse publishTask(@PathVariable UUID id) {
		return adminTaskService.publishTask(id);
	}

	@PostMapping("/{id}/archive")
	TaskResponse archiveTask(@PathVariable UUID id) {
		return adminTaskService.archiveTask(id);
	}

	@PostMapping("/{id}/test-cases")
	TaskTestCaseResponse addTestCase(
			@PathVariable UUID id,
			@Valid @RequestBody TaskTestCaseRequest request) {
		return adminTaskService.addTestCase(id, request);
	}
}
