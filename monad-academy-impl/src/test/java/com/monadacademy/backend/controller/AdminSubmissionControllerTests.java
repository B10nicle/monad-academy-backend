package com.monadacademy.backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.monadacademy.backend.AbstractPostgresTest;
import com.monadacademy.backend.entity.Submission;
import com.monadacademy.backend.entity.SubmissionStatus;
import com.monadacademy.backend.entity.Task;
import com.monadacademy.backend.entity.TaskDifficulty;
import com.monadacademy.backend.entity.TaskStatus;
import com.monadacademy.backend.entity.TaskTopic;
import com.monadacademy.backend.entity.User;
import com.monadacademy.backend.entity.UserRole;
import com.monadacademy.backend.repository.SubmissionRepository;
import com.monadacademy.backend.repository.TaskRepository;
import com.monadacademy.backend.repository.TaskTestCaseRepository;
import com.monadacademy.backend.repository.UserRepository;
import com.monadacademy.backend.repository.UserTaskProgressRepository;
import com.monadacademy.backend.security.JwtTokenService;

/**
 * Verifies admin submission visibility with filtering and pagination.
 *
 * @author Monad Academy Agent
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminSubmissionControllerTests extends AbstractPostgresTest {

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

	@BeforeEach
	void setUp() {
		progressRepository.deleteAll();
		submissionRepository.deleteAll();
		testCaseRepository.deleteAll();
		taskRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	void testListSubmissionsWhenAdminShouldApplyFiltersAndPagination() throws Exception {
		var admin = createActiveAdmin();
		var user = createActiveUser("user@example.com", "user");
		var otherUser = createActiveUser("other@example.com", "other");
		var task = taskRepository.save(createTask("stream-map-values"));
		var otherTask = taskRepository.save(createTask("stream-filter-values"));
		submissionRepository.save(createSubmission(user, task, "accepted", SubmissionStatus.ACCEPTED));
		submissionRepository.save(createSubmission(user, otherTask, "wrong", SubmissionStatus.WRONG_ANSWER));
		submissionRepository.save(createSubmission(otherUser, task, "other", SubmissionStatus.ACCEPTED));

		mockMvc.perform(get("/api/admin/submissions")
						.header("Authorization", "Bearer " + token(admin))
						.param("userId", user.getId().toString())
						.param("taskId", task.getId().toString())
						.param("status", "ACCEPTED")
						.param("page", "-1")
						.param("size", "500"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(1))
				.andExpect(jsonPath("$.content[0].userId").value(user.getId().toString()))
				.andExpect(jsonPath("$.content[0].taskId").value(task.getId().toString()))
				.andExpect(jsonPath("$.content[0].sourceCode").value("accepted"))
				.andExpect(jsonPath("$.content[0].status").value("ACCEPTED"))
				.andExpect(jsonPath("$.page").value(0))
				.andExpect(jsonPath("$.size").value(100))
				.andExpect(jsonPath("$.totalElements").value(1));
	}

	@Test
	void testListUserSubmissionsWhenAdminShouldReturnOnlyRequestedUserSubmissions() throws Exception {
		var admin = createActiveAdmin();
		var user = createActiveUser("user@example.com", "user");
		var otherUser = createActiveUser("other@example.com", "other");
		var task = taskRepository.save(createTask("stream-map-values"));
		submissionRepository.save(createSubmission(user, task, "accepted", SubmissionStatus.ACCEPTED));
		submissionRepository.save(createSubmission(user, task, "wrong", SubmissionStatus.WRONG_ANSWER));
		submissionRepository.save(createSubmission(otherUser, task, "other", SubmissionStatus.WRONG_ANSWER));

		mockMvc.perform(get("/api/admin/users/{userId}/submissions", user.getId())
						.header("Authorization", "Bearer " + token(admin))
						.param("status", "WRONG_ANSWER"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(1))
				.andExpect(jsonPath("$.content[0].userId").value(user.getId().toString()))
				.andExpect(jsonPath("$.content[0].sourceCode").value("wrong"))
				.andExpect(jsonPath("$.totalElements").value(1));
	}

	@Test
	void testListSubmissionsWhenUserShouldReturnForbidden() throws Exception {
		mockMvc.perform(get("/api/admin/submissions")
						.header("Authorization", "Bearer " + token(createActiveUser("user@example.com", "user"))))
				.andExpect(status().isForbidden());
	}

	@Test
	void testListUserSubmissionsWhenUserDoesNotExistShouldReturnNotFound() throws Exception {
		mockMvc.perform(get("/api/admin/users/{userId}/submissions", 999999L)
						.header("Authorization", "Bearer " + token(createActiveAdmin())))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
	}

	private String token(User user) {
		return jwtTokenService.createToken(user);
	}

	private User createActiveAdmin() {
		var user = new User(
				"admin@example.com",
				"admin",
				passwordEncoder.encode("password"),
				UserRole.ADMIN);
		user.activate();
		return userRepository.save(user);
	}

	private User createActiveUser(String email, String username) {
		var user = new User(email, username, passwordEncoder.encode("password"));
		user.activate();
		return userRepository.save(user);
	}

	private Submission createSubmission(User user, Task task, String sourceCode, SubmissionStatus status) {
		var submission = new Submission(user, task, sourceCode);
		submission.complete(status, "{\"status\":\"%s\"}".formatted(status), 42);
		return submission;
	}

	private Task createTask(String slug) {
		return new Task(
				"Map values",
				slug,
				"Use Stream API to map values.",
				"mapValues",
				"String",
				"String input",
				TaskDifficulty.EASY,
				TaskTopic.STREAM_API,
				TaskStatus.PUBLISHED,
				"class Solution { public String mapValues(String input) { return input; } }",
				"class Solution { public String mapValues(String input) { return input.toUpperCase(); } }");
	}
}
