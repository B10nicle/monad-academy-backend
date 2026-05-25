package com.monadacademy.backend.service.task;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.monadacademy.backend.dto.PageResponse;
import com.monadacademy.backend.dto.PublicTaskResponse;
import com.monadacademy.backend.dto.PublicTaskSummaryResponse;
import com.monadacademy.backend.entity.TaskStatus;
import com.monadacademy.backend.exception.AppException;
import com.monadacademy.backend.exception.ErrorCode;
import com.monadacademy.backend.mapper.TaskMapper;
import com.monadacademy.backend.repository.TaskRepository;
import com.monadacademy.backend.repository.TaskTestCaseRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Provides public browsing access to published coding tasks.
 *
 * @author Monad Academy Agent
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PublicTaskService {

	private static final int MAX_PAGE_SIZE = 100;

	private final TaskMapper taskMapper;
	private final TaskRepository taskRepository;
	private final TaskTestCaseRepository testCaseRepository;

	@Transactional(readOnly = true)
	public PageResponse<PublicTaskSummaryResponse> listTasks(int page, int size) {
		var pageable = PageRequest.of(normalizePage(page), normalizeSize(size), Sort.by("createdAt").descending());
		var tasks = taskRepository.findByStatus(TaskStatus.PUBLISHED, pageable);
		var content = tasks.getContent().stream()
				.map(taskMapper::toPublicSummaryResponse)
				.toList();
		log.debug("Loaded published task page={} size={} resultCount={}", page, size, content.size());
		return new PageResponse<>(content, tasks.getNumber(), tasks.getSize(), tasks.getTotalElements(), tasks.getTotalPages());
	}

	@Transactional(readOnly = true)
	public PublicTaskResponse getTask(String slug) {
		var task = taskRepository.findBySlugAndStatus(slug, TaskStatus.PUBLISHED)
				.orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND, HttpStatus.NOT_FOUND));
		var testCases = testCaseRepository.findByTaskAndHiddenFalseOrderByOrderIndexAsc(task);
		log.debug("Loaded published task id={} slug={} publicTestCaseCount={}", task.getId(), task.getSlug(), testCases.size());
		return taskMapper.toPublicResponse(task, testCases);
	}

	private int normalizePage(int page) {
		return Math.max(page, 0);
	}

	private int normalizeSize(int size) {
		if (size < 1) {
			return 20;
		}
		return Math.min(size, MAX_PAGE_SIZE);
	}
}
