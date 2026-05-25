package com.monadacademy.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.monadacademy.backend.dto.PageResponse;
import com.monadacademy.backend.dto.PublicTaskResponse;
import com.monadacademy.backend.dto.PublicTaskSummaryResponse;
import com.monadacademy.backend.service.task.PublicTaskService;

import lombok.RequiredArgsConstructor;

/**
 * Exposes public endpoints for published task browsing.
 *
 * @author Monad Academy Agent
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tasks")
public class PublicTaskController {

	private final PublicTaskService publicTaskService;

	@GetMapping
	PageResponse<PublicTaskSummaryResponse> listTasks(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return publicTaskService.listTasks(page, size);
	}

	@GetMapping("/{slug}")
	PublicTaskResponse getTask(@PathVariable String slug) {
		return publicTaskService.getTask(slug);
	}
}
