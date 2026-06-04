package com.monadacademy.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.monadacademy.backend.AbstractPostgresTest;
import com.monadacademy.backend.entity.AuditEventType;
import com.monadacademy.backend.entity.Task;
import com.monadacademy.backend.entity.TaskDifficulty;
import com.monadacademy.backend.entity.TaskStatus;
import com.monadacademy.backend.entity.TaskTopic;
import com.monadacademy.backend.entity.User;
import com.monadacademy.backend.entity.UserRole;
import com.monadacademy.backend.repository.AuditLogRepository;
import com.monadacademy.backend.repository.TaskRepository;
import com.monadacademy.backend.repository.TaskTestCaseRepository;
import com.monadacademy.backend.repository.UserRepository;
import com.monadacademy.backend.security.JwtTokenService;

/**
 * Verifies admin task management API flows with PostgreSQL Testcontainers.
 *
 * @author Monad Academy Agent
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminTaskControllerTests extends AbstractPostgresTest {

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
	AuditLogRepository auditLogRepository;

	@Autowired
	TaskTestCaseRepository testCaseRepository;

	@BeforeEach
	void setUp() {
		auditLogRepository.deleteAll();
		testCaseRepository.deleteAll();
		taskRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	void testCreateTaskWhenAdminShouldPersistTaskWithTestCases() throws Exception {
		mockMvc.perform(post("/api/admin/tasks")
						.header("Authorization", "Bearer " + token(createActiveAdmin()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(taskRequest("stream-filter-active-users", "DRAFT")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.slug").value("stream-filter-active-users"))
				.andExpect(jsonPath("$.methodName").value("mapValues"))
				.andExpect(jsonPath("$.methodReturnType").value("String"))
				.andExpect(jsonPath("$.methodParameters").value("String input"))
				.andExpect(jsonPath("$.status").value("DRAFT"))
				.andExpect(jsonPath("$.testCases[0].input").value("[true,false]"));

		var task = taskRepository.findBySlug("stream-filter-active-users").orElseThrow();
		var auditEvents = auditLogRepository.findAll().stream()
				.map(auditLog -> auditLog.getEventType())
				.toList();
		assertThat(task.getMethodName()).isEqualTo("mapValues");
		assertThat(task.getMethodReturnType()).isEqualTo("String");
		assertThat(task.getMethodParameters()).isEqualTo("String input");
		assertThat(testCaseRepository.findByTaskOrderByOrderIndexAsc(task)).hasSize(1);
		assertThat(auditEvents).contains(AuditEventType.TASK_CREATED);
	}

	@Test
	void testCreateTaskWhenSignatureCannotBeResolvedShouldReturnBadRequest() throws Exception {
		mockMvc.perform(post("/api/admin/tasks")
						.header("Authorization", "Bearer " + token(createActiveAdmin()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(taskRequestWithCode("stream-invalid-task", "class Solution {}", "class Solution {}")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
	}

	@Test
	void testCreateTaskWhenExplicitSignatureIsBlankShouldResolveFromCode() throws Exception {
		mockMvc.perform(post("/api/admin/tasks")
						.header("Authorization", "Bearer " + token(createActiveAdmin()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(taskRequestWithBlankSignature("stream-blank-signature")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.methodName").value("mapValues"));
	}

	@Test
	void testCreateTaskWhenUserShouldReturnForbidden() throws Exception {
		mockMvc.perform(post("/api/admin/tasks")
						.header("Authorization", "Bearer " + token(createActiveUser()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(taskRequest("stream-filter-active-users", "DRAFT")))
				.andExpect(status().isForbidden());
	}

	@Test
	void testUpdateTaskWhenAdminShouldUpdateTaskContent() throws Exception {
		var task = taskRepository.save(createTask("stream-filter-active-users"));

		mockMvc.perform(put("/api/admin/tasks/{id}", task.getId())
						.header("Authorization", "Bearer " + token(createActiveAdmin()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(taskRequest("stream-map-usernames", "DRAFT")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.slug").value("stream-map-usernames"));

		var updatedTask = taskRepository.findById(task.getId()).orElseThrow();
		assertThat(updatedTask.getSlug()).isEqualTo("stream-map-usernames");
		assertThat(updatedTask.getTitle()).isEqualTo("Filter active users");
		assertThat(updatedTask.getMethodName()).isEqualTo("mapValues");
	}

	@Test
	void testPublishAndArchiveTaskWhenAdminShouldChangeStatus() throws Exception {
		var task = taskRepository.save(createTask("stream-filter-active-users"));
		var token = token(createActiveAdmin());

		mockMvc.perform(post("/api/admin/tasks/{id}/publish", task.getId())
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PUBLISHED"));

		mockMvc.perform(post("/api/admin/tasks/{id}/archive", task.getId())
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ARCHIVED"));

		var archivedTask = taskRepository.findById(task.getId()).orElseThrow();
		assertThat(archivedTask.getStatus()).isEqualTo(TaskStatus.ARCHIVED);
	}

	@Test
	void testAddTestCaseWhenAdminShouldPersistTestCase() throws Exception {
		var task = taskRepository.save(createTask("stream-filter-active-users"));

		mockMvc.perform(post("/api/admin/tasks/{id}/test-cases", task.getId())
						.header("Authorization", "Bearer " + token(createActiveAdmin()))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "input": "[1,2,3]",
								  "expectedOutput": "[2,4,6]",
								  "hidden": true,
								  "orderIndex": 3
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.hidden").value(true))
				.andExpect(jsonPath("$.orderIndex").value(3));

		assertThat(testCaseRepository.findByTaskOrderByOrderIndexAsc(task)).hasSize(1);
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

	private User createActiveUser() {
		var user = new User("user@example.com", "user", passwordEncoder.encode("password"));
		user.activate();
		return userRepository.save(user);
	}

	private Task createTask(String slug) {
		return new Task(
				"Filter active users",
				slug,
				"Use Stream API to filter active users.",
				"mapValues",
				"String",
				"String input",
				TaskDifficulty.EASY,
				TaskTopic.STREAM_API,
				TaskStatus.DRAFT,
				"class Solution { public String mapValues(String input) { return input; } }",
				"class Solution { public String mapValues(String input) { return input.trim(); } }");
	}

	private String taskRequest(String slug, String status) {
		return """
				{
				  "title": "Filter active users",
				  "slug": "%s",
				  "description": "Use Stream API to filter active users.",
				  "difficulty": "EASY",
				  "topic": "STREAM_API",
				  "status": "%s",
				  "initialCode": "class Solution { public String mapValues(String input) { return input; } }",
				  "solutionTemplate": "class Solution { public String mapValues(String input) { return input.trim(); } }",
				  "testCases": [
				    {
				      "input": "[true,false]",
				      "expectedOutput": "[true]",
				      "hidden": false,
				      "orderIndex": 0
				    }
				  ]
				}
				""".formatted(slug, status);
	}

	private String taskRequestWithCode(String slug, String initialCode, String solutionTemplate) {
		return """
				{
				  "title": "Filter active users",
				  "slug": "%s",
				  "description": "Use Stream API to filter active users.",
				  "difficulty": "EASY",
				  "topic": "STREAM_API",
				  "status": "DRAFT",
				  "initialCode": "%s",
				  "solutionTemplate": "%s",
				  "testCases": []
				}
				""".formatted(slug, initialCode, solutionTemplate);
	}

	private String taskRequestWithBlankSignature(String slug) {
		return """
				{
				  "title": "Filter active users",
				  "slug": "%s",
				  "description": "Use Stream API to filter active users.",
				  "methodName": "",
				  "methodReturnType": "",
				  "methodParameters": "",
				  "difficulty": "EASY",
				  "topic": "STREAM_API",
				  "status": "DRAFT",
				  "initialCode": "class Solution { public String mapValues(String input) { return input; } }",
				  "solutionTemplate": "class Solution { public String mapValues(String input) { return input.trim(); } }",
				  "testCases": []
				}
				""".formatted(slug);
	}
}
