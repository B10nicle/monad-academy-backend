package com.monadacademy.backend.service.task;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.monadacademy.backend.dto.TaskRequest;
import com.monadacademy.backend.dto.TaskResponse;
import com.monadacademy.backend.dto.TaskTestCaseRequest;
import com.monadacademy.backend.dto.TaskTestCaseResponse;
import com.monadacademy.backend.entity.AuditEventType;
import com.monadacademy.backend.entity.Task;
import com.monadacademy.backend.entity.TaskTestCase;
import com.monadacademy.backend.entity.User;
import com.monadacademy.backend.exception.AppException;
import com.monadacademy.backend.exception.ErrorCode;
import com.monadacademy.backend.mapper.TaskMapper;
import com.monadacademy.backend.repository.TaskRepository;
import com.monadacademy.backend.repository.TaskTestCaseRepository;
import com.monadacademy.backend.repository.UserRepository;
import com.monadacademy.backend.security.CurrentUserProvider;
import com.monadacademy.backend.service.audit.AuditLogService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Orchestrates admin task management and task lifecycle operations.
 *
 * @author Monad Academy Agent
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminTaskService {

	private final TaskMapper taskMapper;
	private final UserRepository userRepository;
	private final TaskRepository taskRepository;
	private final AuditLogService auditLogService;
	private final CurrentUserProvider currentUserProvider;
	private final TaskTestCaseRepository testCaseRepository;
	private final TaskMethodSignatureResolver signatureResolver;

	@Transactional
	public TaskResponse createTask(TaskRequest request) {
		validateUniqueSlug(request.slug(), null);
		var signature = resolveSignature(request);
		var task = taskRepository.save(new Task(
				request.title(),
				request.slug(),
				request.description(),
				signature.methodName(),
				signature.methodReturnType(),
				signature.methodParameters(),
				request.difficulty(),
				request.topic(),
				request.status(),
				request.initialCode(),
				request.solutionTemplate()));
		if (request.testCases() != null) {
			request.testCases()
					.forEach(testCaseRequest -> testCaseRepository.save(toTestCase(task, testCaseRequest)));
		}
		auditLogService.log(currentUser(), AuditEventType.TASK_CREATED, metadata(task));
		log.debug("Created task id={} slug={}", task.getId(), task.getSlug());
		return taskMapper.toResponse(task, testCaseRepository.findByTaskOrderByOrderIndexAsc(task));
	}

	@Transactional
	public TaskResponse updateTask(Long id, TaskRequest request) {
		var task = findTask(id);
		validateUniqueSlug(request.slug(), task.getId());
		var signature = resolveSignature(request);
		task.update(
				request.title(),
				request.slug(),
				request.description(),
				signature.methodName(),
				signature.methodReturnType(),
				signature.methodParameters(),
				request.difficulty(),
				request.topic(),
				request.status(),
				request.initialCode(),
				request.solutionTemplate());
		auditLogService.log(currentUser(), AuditEventType.TASK_UPDATED, metadata(task));
		log.debug("Updated task id={} slug={}", task.getId(), task.getSlug());
		return taskMapper.toResponse(task, testCaseRepository.findByTaskOrderByOrderIndexAsc(task));
	}

	@Transactional
	public TaskResponse publishTask(Long id) {
		var task = findTask(id);
		task.publish();
		auditLogService.log(currentUser(), AuditEventType.TASK_PUBLISHED, metadata(task));
		log.debug("Published task id={} slug={}", task.getId(), task.getSlug());
		return taskMapper.toResponse(task, testCaseRepository.findByTaskOrderByOrderIndexAsc(task));
	}

	@Transactional
	public TaskResponse archiveTask(Long id) {
		var task = findTask(id);
		task.archive();
		auditLogService.log(currentUser(), AuditEventType.TASK_ARCHIVED, metadata(task));
		log.debug("Archived task id={} slug={}", task.getId(), task.getSlug());
		return taskMapper.toResponse(task, testCaseRepository.findByTaskOrderByOrderIndexAsc(task));
	}

	@Transactional
	public TaskTestCaseResponse addTestCase(Long taskId, TaskTestCaseRequest request) {
		var task = findTask(taskId);
		var testCase = testCaseRepository.save(toTestCase(task, request));
		auditLogService.log(currentUser(), AuditEventType.TASK_TEST_CASE_CREATED, metadata(task));
		log.debug("Created test case id={} for taskId={}", testCase.getId(), task.getId());
		return taskMapper.toResponse(testCase);
	}

	private Task findTask(Long id) {
		return taskRepository.findById(id)
				.orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND, HttpStatus.NOT_FOUND));
	}

	private TaskTestCase toTestCase(Task task, TaskTestCaseRequest request) {
		return new TaskTestCase(
				task,
				request.input(),
				request.expectedOutput(),
				request.hidden(),
				request.orderIndex());
	}

	private TaskMethodSignature resolveSignature(TaskRequest request) {
		var initialSignature = signatureResolver.resolve(request.initialCode());
		var solutionSignature = signatureResolver.resolve(request.solutionTemplate());
		initialSignature.ifPresent(signature -> solutionSignature.ifPresent(solution -> validateSignatureMatchesSource(solution, signature)));
		var parsedSignature = initialSignature
				.or(() -> solutionSignature)
				.orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST));
		var methodName = explicitOrParsed(request.methodName(), parsedSignature.methodName());
		var methodReturnType = explicitOrParsed(request.methodReturnType(), parsedSignature.methodReturnType());
		var methodParameters = explicitOrParsed(request.methodParameters(), parsedSignature.methodParameters());
		var signature = new TaskMethodSignature(methodName, methodReturnType, methodParameters);
		validateSignatureMatchesSource(signature, parsedSignature);
		return signature;
	}

	private String explicitOrParsed(String explicitValue, String parsedValue) {
		if (explicitValue == null || explicitValue.isBlank()) {
			return parsedValue;
		}
		return explicitValue.trim();
	}

	private void validateSignatureMatchesSource(TaskMethodSignature signature, TaskMethodSignature parsedSignature) {
		if (!signature.equals(parsedSignature)) {
			throw new AppException(ErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST);
		}
	}

	private User currentUser() {
		var currentUser = currentUserProvider.getCurrentUser();
		if (currentUser == null) {
			return null;
		}
		return userRepository.findById(currentUser.id()).orElse(null);
	}

	private void validateUniqueSlug(String slug, Long currentTaskId) {
		taskRepository.findBySlug(slug)
				.filter(task -> !task.getId().equals(currentTaskId))
				.ifPresent(task -> {
					throw new AppException(ErrorCode.TASK_SLUG_ALREADY_EXISTS, HttpStatus.CONFLICT);
				});
	}

	private String metadata(Task task) {
		return "taskId=" + task.getId() + ",slug=" + task.getSlug();
	}
}
