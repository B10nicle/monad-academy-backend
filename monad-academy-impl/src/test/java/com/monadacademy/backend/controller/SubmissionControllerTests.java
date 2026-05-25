package com.monadacademy.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.monadacademy.backend.AbstractPostgresTest;
import com.monadacademy.backend.entity.Submission;
import com.monadacademy.backend.entity.SubmissionStatus;
import com.monadacademy.backend.entity.Task;
import com.monadacademy.backend.entity.TaskDifficulty;
import com.monadacademy.backend.entity.TaskStatus;
import com.monadacademy.backend.entity.TaskTestCase;
import com.monadacademy.backend.entity.TaskTopic;
import com.monadacademy.backend.entity.User;
import com.monadacademy.backend.repository.SubmissionRepository;
import com.monadacademy.backend.repository.TaskRepository;
import com.monadacademy.backend.repository.TaskTestCaseRepository;
import com.monadacademy.backend.repository.UserRepository;
import com.monadacademy.backend.repository.UserTaskProgressRepository;
import com.monadacademy.backend.security.JwtTokenService;
import com.monadacademy.backend.service.runner.JavaCodeRunRequest;
import com.monadacademy.backend.service.runner.JavaCodeRunResult;
import com.monadacademy.backend.service.runner.JavaCodeRunner;

/**
 * Verifies authenticated submission API flows with PostgreSQL Testcontainers.
 *
 * @author Monad Academy Agent
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class SubmissionControllerTests extends AbstractPostgresTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JwtTokenService jwtTokenService;

	@Autowired
	UserRepository userRepository;

	@Autowired
	TaskRepository taskRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	SubmissionRepository submissionRepository;

	@Autowired
	TaskTestCaseRepository testCaseRepository;

	@Autowired
	UserTaskProgressRepository progressRepository;

	@Autowired
	FakeJavaCodeRunner javaCodeRunner;

	@BeforeEach
	void setUp() {
		progressRepository.deleteAll();
		submissionRepository.deleteAll();
		testCaseRepository.deleteAll();
		taskRepository.deleteAll();
		userRepository.deleteAll();
		javaCodeRunner.reset();
	}

	@Test
	void testCreateSubmissionWhenUnauthenticatedShouldReturnUnauthorized() throws Exception {
		var task = taskRepository.save(createTask("stream-map-values", TaskStatus.PUBLISHED));

		mockMvc.perform(post("/api/submissions")
						.contentType(MediaType.APPLICATION_JSON)
						.content(submissionRequest(task.getId().toString(), "return input;")))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void testCreateSubmissionWhenAuthenticatedShouldExecuteAndPersistResult() throws Exception {
		var user = createActiveUser("user@example.com", "user");
		var task = taskRepository.save(createTask("stream-map-values", TaskStatus.PUBLISHED));
		testCaseRepository.save(new TaskTestCase(task, "a", "A", false, 0));
		testCaseRepository.save(new TaskTestCase(task, "b", "B", true, 1));

		mockMvc.perform(post("/api/submissions")
						.header("Authorization", "Bearer " + token(user))
						.contentType(MediaType.APPLICATION_JSON)
						.content(submissionRequest(task.getId().toString(), "return input.toUpperCase();")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.taskId").value(task.getId().toString()))
				.andExpect(jsonPath("$.userId").value(user.getId().toString()))
				.andExpect(jsonPath("$.sourceCode").value("return input.toUpperCase();"))
				.andExpect(jsonPath("$.status").value("ACCEPTED"))
				.andExpect(jsonPath("$.executionMetadata").value("{\"status\":\"ACCEPTED\"}"))
				.andExpect(jsonPath("$.executionDurationMs").value(42));

		var submission = submissionRepository.findAll().getFirst();
		var progress = progressRepository.findByUserAndTask(user, task).orElseThrow();
		assertThat(submission.getStatus()).isEqualTo(SubmissionStatus.ACCEPTED);
		assertThat(progress.getAttemptsCount()).isEqualTo(1);
		assertThat(progress.getSolvedAt()).isNotNull();
		assertThat(javaCodeRunner.lastRequest().sourceCode()).isEqualTo("return input.toUpperCase();");
		assertThat(javaCodeRunner.lastRequest().testCases()).hasSize(2);
	}

	@Test
	void testCreateSubmissionWhenTaskIsDraftShouldReturnNotFound() throws Exception {
		var user = createActiveUser("user@example.com", "user");
		var task = taskRepository.save(createTask("stream-map-values", TaskStatus.DRAFT));

		mockMvc.perform(post("/api/submissions")
						.header("Authorization", "Bearer " + token(user))
						.contentType(MediaType.APPLICATION_JSON)
						.content(submissionRequest(task.getId().toString(), "return input;")))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("TASK_NOT_FOUND"));
	}

	@Test
	void testListCurrentUserSubmissionsShouldReturnOnlyCurrentUserHistory() throws Exception {
		var user = createActiveUser("user@example.com", "user");
		var otherUser = createActiveUser("other@example.com", "other");
		var task = taskRepository.save(createTask("stream-map-values", TaskStatus.PUBLISHED));
		submissionRepository.save(new Submission(user, task, "first"));
		submissionRepository.save(new Submission(user, task, "second"));
		submissionRepository.save(new Submission(otherUser, task, "other"));

		mockMvc.perform(get("/api/submissions/my")
						.header("Authorization", "Bearer " + token(user))
						.param("page", "0")
						.param("size", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(2))
				.andExpect(jsonPath("$.totalElements").value(2));
	}

	@Test
	void testListCurrentUserTaskSubmissionsShouldReturnOnlyTaskHistory() throws Exception {
		var user = createActiveUser("user@example.com", "user");
		var task = taskRepository.save(createTask("stream-map-values", TaskStatus.PUBLISHED));
		var otherTask = taskRepository.save(createTask("stream-filter-values", TaskStatus.PUBLISHED));
		submissionRepository.save(new Submission(user, task, "task submission"));
		submissionRepository.save(new Submission(user, otherTask, "other task submission"));

		mockMvc.perform(get("/api/tasks/{taskId}/submissions/my", task.getId())
						.header("Authorization", "Bearer " + token(user))
						.param("page", "-1")
						.param("size", "500"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(1))
				.andExpect(jsonPath("$.content[0].sourceCode").value("task submission"))
				.andExpect(jsonPath("$.page").value(0))
				.andExpect(jsonPath("$.size").value(100))
				.andExpect(jsonPath("$.totalElements").value(1));
	}

	private String token(User user) {
		return jwtTokenService.createToken(user);
	}

	private User createActiveUser(String email, String username) {
		var user = new User(email, username, passwordEncoder.encode("password"));
		user.activate();
		return userRepository.save(user);
	}

	private Task createTask(String slug, TaskStatus status) {
		return new Task(
				"Map values",
				slug,
				"Use Stream API to map values.",
				TaskDifficulty.EASY,
				TaskTopic.STREAM_API,
				status,
				"return values.stream();",
				"return values.stream().map(String::toUpperCase).toList();");
	}

	private String submissionRequest(String taskId, String sourceCode) {
		return """
				{
				  "taskId": "%s",
				  "sourceCode": "%s"
				}
				""".formatted(taskId, sourceCode);
	}

	/**
	 * Provides a deterministic runner for submission controller tests.
	 *
	 * @author Monad Academy Agent
	 */
	@TestConfiguration
	static class RunnerTestConfiguration {

		@Bean
		@Primary
		FakeJavaCodeRunner javaCodeRunner() {
			return new FakeJavaCodeRunner();
		}
	}

	/**
	 * Captures runner requests and returns a stable accepted result.
	 *
	 * @author Monad Academy Agent
	 */
	static class FakeJavaCodeRunner implements JavaCodeRunner {

		private JavaCodeRunRequest lastRequest;

		@Override
		public JavaCodeRunResult run(JavaCodeRunRequest request) {
			this.lastRequest = request;
			return new JavaCodeRunResult(SubmissionStatus.ACCEPTED, "", "", "{\"status\":\"ACCEPTED\"}", 42);
		}

		JavaCodeRunRequest lastRequest() {
			return lastRequest;
		}

		void reset() {
			this.lastRequest = null;
		}
	}
}
