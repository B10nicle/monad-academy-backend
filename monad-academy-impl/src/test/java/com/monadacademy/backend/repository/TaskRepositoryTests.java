package com.monadacademy.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.monadacademy.backend.AbstractPostgresTest;
import com.monadacademy.backend.entity.Task;
import com.monadacademy.backend.entity.TaskDifficulty;
import com.monadacademy.backend.entity.TaskStatus;
import com.monadacademy.backend.entity.TaskTestCase;
import com.monadacademy.backend.entity.TaskTopic;

/**
 * Verifies task and task test case persistence with PostgreSQL Testcontainers.
 *
 * @author Monad Academy Agent
 */
@SpringBootTest
@ActiveProfiles("test")
class TaskRepositoryTests extends AbstractPostgresTest {

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
	void testSaveWhenTaskProvidedShouldPersistTask() {
		var task = taskRepository.save(createTask("stream-filter-active-users"));

		var foundTask = taskRepository.findBySlug("stream-filter-active-users").orElseThrow();

		assertThat(taskRepository.existsBySlug("stream-filter-active-users")).isTrue();
		assertThat(foundTask.getId()).isEqualTo(task.getId());
		assertThat(foundTask.getDifficulty()).isEqualTo(TaskDifficulty.EASY);
		assertThat(foundTask.getTopic()).isEqualTo(TaskTopic.STREAM_API);
		assertThat(foundTask.getStatus()).isEqualTo(TaskStatus.DRAFT);
		assertThat(foundTask.getCreatedAt()).isNotNull();
		assertThat(foundTask.getUpdatedAt()).isNotNull();
	}

	@Test
	void testFindByTaskOrderByOrderIndexAscShouldReturnOrderedTestCases() {
		var task = taskRepository.save(createTask("stream-map-usernames"));
		testCaseRepository.save(new TaskTestCase(task, "[\"Oleg\"]", "[\"oleg\"]", false, 2));
		testCaseRepository.save(new TaskTestCase(task, "[\"Ada\"]", "[\"ada\"]", true, 1));

		var testCases = testCaseRepository.findByTaskOrderByOrderIndexAsc(task);

		assertThat(testCases).hasSize(2);
		assertThat(testCases.getFirst().getOrderIndex()).isEqualTo(1);
		assertThat(testCases.getFirst().isHidden()).isTrue();
		assertThat(testCases.getLast().getOrderIndex()).isEqualTo(2);
		assertThat(testCases.getLast().getCreatedAt()).isNotNull();
	}

	private Task createTask(String slug) {
		return new Task(
				"Filter active users",
				slug,
				"Use Stream API to filter active users.",
				TaskDifficulty.EASY,
				TaskTopic.STREAM_API,
				TaskStatus.DRAFT,
				"return users.stream();",
				"return users.stream().filter(User::active).toList();");
	}
}
