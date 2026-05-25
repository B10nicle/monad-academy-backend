package com.monadacademy.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.monadacademy.backend.AbstractPostgresTest;
import com.monadacademy.backend.entity.Task;
import com.monadacademy.backend.entity.TaskDifficulty;
import com.monadacademy.backend.entity.TaskStatus;
import com.monadacademy.backend.entity.TaskTestCase;
import com.monadacademy.backend.entity.TaskTopic;
import com.monadacademy.backend.repository.TaskRepository;
import com.monadacademy.backend.repository.TaskTestCaseRepository;

/**
 * Verifies public published task browsing API flows with PostgreSQL Testcontainers.
 *
 * @author Monad Academy Agent
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PublicTaskControllerTests extends AbstractPostgresTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	TaskRepository taskRepository;

	@Autowired
	TaskTestCaseRepository testCaseRepository;

	@BeforeEach
	void setUp() {
		testCaseRepository.deleteAll();
		taskRepository.deleteAll();
	}

	@Test
	void testListTasksWhenAnonymousShouldReturnOnlyPublishedTasks() throws Exception {
		taskRepository.save(createTask("published-task", TaskStatus.PUBLISHED));
		taskRepository.save(createTask("draft-task", TaskStatus.DRAFT));
		taskRepository.save(createTask("archived-task", TaskStatus.ARCHIVED));

		mockMvc.perform(get("/api/tasks")
						.param("page", "0")
						.param("size", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(1))
				.andExpect(jsonPath("$.content[0].slug").value("published-task"))
				.andExpect(jsonPath("$.content[0].difficulty").value("EASY"))
				.andExpect(jsonPath("$.content[0].topic").value("STREAM_API"))
				.andExpect(jsonPath("$.content[0].description").doesNotExist())
				.andExpect(jsonPath("$.totalElements").value(1));
	}

	@Test
	void testListTasksWhenSizeIsTooLargeShouldCapPageSize() throws Exception {
		taskRepository.save(createTask("published-task", TaskStatus.PUBLISHED));

		mockMvc.perform(get("/api/tasks")
						.param("page", "-1")
						.param("size", "500"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.page").value(0))
				.andExpect(jsonPath("$.size").value(100));
	}

	@Test
	void testGetTaskWhenPublishedShouldReturnDetailsWithPublicTestCases() throws Exception {
		var task = taskRepository.save(createTask("published-task", TaskStatus.PUBLISHED));
		testCaseRepository.save(new TaskTestCase(task, "[1,2,3]", "[2,4,6]", false, 2));
		testCaseRepository.save(new TaskTestCase(task, "[4,5,6]", "[8,10,12]", true, 1));

		mockMvc.perform(get("/api/tasks/{slug}", "published-task"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.slug").value("published-task"))
				.andExpect(jsonPath("$.description").value("Use Stream API to transform values."))
				.andExpect(jsonPath("$.initialCode").value("return values.stream();"))
				.andExpect(jsonPath("$.solutionTemplate").doesNotExist())
				.andExpect(jsonPath("$.status").doesNotExist())
				.andExpect(jsonPath("$.testCases.length()").value(1))
				.andExpect(jsonPath("$.testCases[0].input").value("[1,2,3]"))
				.andExpect(jsonPath("$.testCases[0].hidden").doesNotExist());

		assertThat(testCaseRepository.findByTaskAndHiddenFalseOrderByOrderIndexAsc(task)).hasSize(1);
	}

	@Test
	void testGetTaskWhenTaskIsNotPublishedShouldReturnNotFound() throws Exception {
		taskRepository.save(createTask("draft-task", TaskStatus.DRAFT));

		mockMvc.perform(get("/api/tasks/{slug}", "draft-task"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("TASK_NOT_FOUND"));
	}

	private Task createTask(String slug, TaskStatus status) {
		return new Task(
				"Transform values",
				slug,
				"Use Stream API to transform values.",
				TaskDifficulty.EASY,
				TaskTopic.STREAM_API,
				status,
				"return values.stream();",
				"return values.stream().map(value -> value * 2).toList();");
	}
}
