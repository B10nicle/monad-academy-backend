package com.monadacademy.backend.service.submission;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.monadacademy.backend.dto.PageResponse;
import com.monadacademy.backend.dto.SubmissionRequest;
import com.monadacademy.backend.dto.SubmissionResponse;
import com.monadacademy.backend.entity.Submission;
import com.monadacademy.backend.entity.SubmissionStatus;
import com.monadacademy.backend.entity.Task;
import com.monadacademy.backend.entity.TaskStatus;
import com.monadacademy.backend.entity.User;
import com.monadacademy.backend.exception.AppException;
import com.monadacademy.backend.exception.ErrorCode;
import com.monadacademy.backend.mapper.SubmissionMapper;
import com.monadacademy.backend.repository.SubmissionRepository;
import com.monadacademy.backend.repository.TaskRepository;
import com.monadacademy.backend.repository.TaskTestCaseRepository;
import com.monadacademy.backend.repository.UserRepository;
import com.monadacademy.backend.security.CurrentUserProvider;
import com.monadacademy.backend.service.progress.UserTaskProgressService;
import com.monadacademy.backend.service.runner.JavaCodeRunRequest;
import com.monadacademy.backend.service.runner.JavaCodeRunner;
import com.monadacademy.backend.service.runner.JavaCodeRunnerTestCase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Orchestrates authenticated user submission execution and history access.
 *
 * @author Monad Academy Agent
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {

	private static final int MAX_PAGE_SIZE = 100;
	private static final String ID_FIELD = "id";
	private static final String TASK_FIELD = "task";
	private static final String USER_FIELD = "user";
	private static final String STATUS_FIELD = "status";
	private static final String CREATED_AT_FIELD = "createdAt";

	private final JavaCodeRunner javaCodeRunner;
	private final UserRepository userRepository;
	private final TaskRepository taskRepository;
	private final SubmissionMapper submissionMapper;
	private final UserTaskProgressService progressService;
	private final SubmissionRepository submissionRepository;
	private final CurrentUserProvider currentUserProvider;
	private final TaskTestCaseRepository testCaseRepository;

	@Transactional
	public SubmissionResponse createSubmission(SubmissionRequest request) {
		var user = currentUser();
		var task = findPublishedTask(request.taskId());
		var submission = submissionRepository.save(new Submission(user, task, request.sourceCode()));
		submission.markRunning();
		var runResult = javaCodeRunner.run(new JavaCodeRunRequest(
				request.sourceCode(),
				testCaseRepository.findByTaskOrderByOrderIndexAsc(task).stream()
						.map(testCase -> new JavaCodeRunnerTestCase(testCase.getInput(), testCase.getExpectedOutput()))
						.toList()));
		submission.complete(runResult.status(), runResult.executionMetadata(), runResult.executionDurationMs());
		var savedSubmission = submissionRepository.save(submission);
		progressService.recordSubmission(user, task, savedSubmission.getStatus());
		log.debug("Created submission id={} taskId={} userId={} status={}",
				savedSubmission.getId(), task.getId(), user.getId(), savedSubmission.getStatus());
		return submissionMapper.toResponse(savedSubmission);
	}

	@Transactional(readOnly = true)
	public PageResponse<SubmissionResponse> listCurrentUserSubmissions(int page, int size) {
		var user = currentUser();
		var pageable = pageable(page, size);
		var submissions = submissionRepository.findByUser(user, pageable);
		var content = submissions.getContent().stream()
				.map(submissionMapper::toResponse)
				.toList();
		log.debug("Loaded submission history userId={} page={} size={} resultCount={}", user.getId(), page, size, content.size());
		return new PageResponse<>(content, submissions.getNumber(), submissions.getSize(), submissions.getTotalElements(), submissions.getTotalPages());
	}

	@Transactional(readOnly = true)
	public PageResponse<SubmissionResponse> listCurrentUserTaskSubmissions(Long taskId, int page, int size) {
		var user = currentUser();
		var task = findPublishedTask(taskId);
		var pageable = pageable(page, size);
		var submissions = submissionRepository.findByUserAndTask(user, task, pageable);
		var content = submissions.getContent().stream()
				.map(submissionMapper::toResponse)
				.toList();
		log.debug("Loaded task submission history userId={} taskId={} page={} size={} resultCount={}",
				user.getId(), task.getId(), page, size, content.size());
		return new PageResponse<>(content, submissions.getNumber(), submissions.getSize(), submissions.getTotalElements(), submissions.getTotalPages());
	}

	@Transactional(readOnly = true)
	public PageResponse<SubmissionResponse> listAdminSubmissions(
			Long userId,
			Long taskId,
			SubmissionStatus status,
			int page,
			int size) {
		var submissions = submissionRepository.findAll(adminSubmissionSpec(userId, taskId, status), pageable(page, size));
		var response = toPageResponse(submissions);
		log.debug("Loaded admin submissions userId={} taskId={} status={} page={} size={} resultCount={}",
				userId, taskId, status, page, size, response.content().size());
		return response;
	}

	@Transactional(readOnly = true)
	public PageResponse<SubmissionResponse> listAdminUserSubmissions(
			Long userId,
			Long taskId,
			SubmissionStatus status,
			int page,
			int size) {
		if (!userRepository.existsById(userId)) {
			throw new AppException(ErrorCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
		}
		var submissions = submissionRepository.findAll(adminSubmissionSpec(userId, taskId, status), pageable(page, size));
		var response = toPageResponse(submissions);
		log.debug("Loaded admin user submissions userId={} taskId={} status={} page={} size={} resultCount={}",
				userId, taskId, status, page, size, response.content().size());
		return response;
	}

	private User currentUser() {
		var currentUser = currentUserProvider.getCurrentUser();
		if (currentUser == null) {
			throw new AppException(ErrorCode.UNAUTHENTICATED, HttpStatus.UNAUTHORIZED);
		}
		return userRepository.findById(currentUser.id())
				.orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
	}

	private Task findPublishedTask(Long taskId) {
		return taskRepository.findByIdAndStatus(taskId, TaskStatus.PUBLISHED)
				.orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND, HttpStatus.NOT_FOUND));
	}

	private Specification<Submission> adminSubmissionSpec(Long userId, Long taskId, SubmissionStatus status) {
		return (root, query, criteriaBuilder) -> {
			Predicate predicate = criteriaBuilder.conjunction();
			if (userId != null) {
				predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get(USER_FIELD).get(ID_FIELD), userId));
			}
			if (taskId != null) {
				predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get(TASK_FIELD).get(ID_FIELD), taskId));
			}
			if (status != null) {
				predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get(STATUS_FIELD), status));
			}
			return predicate;
		};
	}

	private PageResponse<SubmissionResponse> toPageResponse(Page<Submission> submissions) {
		var content = submissions.getContent().stream()
				.map(submissionMapper::toResponse)
				.toList();
		return new PageResponse<>(content, submissions.getNumber(), submissions.getSize(), submissions.getTotalElements(), submissions.getTotalPages());
	}

	private PageRequest pageable(int page, int size) {
		return PageRequest.of(normalizePage(page), normalizeSize(size), Sort.by(CREATED_AT_FIELD).descending());
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
