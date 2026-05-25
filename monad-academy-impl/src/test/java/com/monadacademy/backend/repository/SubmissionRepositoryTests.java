package com.monadacademy.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import com.monadacademy.backend.AbstractPostgresTest;
import com.monadacademy.backend.entity.Submission;
import com.monadacademy.backend.entity.SubmissionStatus;
import com.monadacademy.backend.entity.Task;
import com.monadacademy.backend.entity.TaskDifficulty;
import com.monadacademy.backend.entity.TaskStatus;
import com.monadacademy.backend.entity.TaskTopic;
import com.monadacademy.backend.entity.User;

/**
 * Verifies submission persistence and history queries with PostgreSQL Testcontainers.
 *
 * @author Monad Academy Agent
 */
@SpringBootTest
@ActiveProfiles("test")
class SubmissionRepositoryTests extends AbstractPostgresTest {

	@Autowired
	UserRepository userRepository;

	@Autowired
	TaskRepository taskRepository;

	@Autowired
	SubmissionRepository submissionRepository;

	@BeforeEach
	void setUp() {
		submissionRepository.deleteAll();
		taskRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	void testSaveWhenSubmissionProvidedShouldPersistPendingSubmission() {
		var user = userRepository.save(new User("user@example.com", "user", "hash"));
		var task = taskRepository.save(createTask("stream-filter-users"));

		var submission = submissionRepository.save(new Submission(user, task, "return users.stream();"));

		var foundSubmission = submissionRepository.findById(submission.getId()).orElseThrow();
		assertThat(foundSubmission.getUser().getId()).isEqualTo(user.getId());
		assertThat(foundSubmission.getTask().getId()).isEqualTo(task.getId());
		assertThat(foundSubmission.getSourceCode()).isEqualTo("return users.stream();");
		assertThat(foundSubmission.getStatus()).isEqualTo(SubmissionStatus.PENDING);
		assertThat(foundSubmission.getExecutionMetadata()).isNull();
		assertThat(foundSubmission.getExecutionDurationMs()).isNull();
		assertThat(foundSubmission.getCreatedAt()).isNotNull();
		assertThat(foundSubmission.getUpdatedAt()).isNotNull();
	}

	@Test
	void testCompleteWhenResultProvidedShouldStoreExecutionMetadata() {
		var user = userRepository.save(new User("user@example.com", "user", "hash"));
		var task = taskRepository.save(createTask("stream-map-users"));
		var submission = submissionRepository.save(new Submission(user, task, "return users.stream();"));

		submission.markRunning();
		submission.complete(SubmissionStatus.ACCEPTED, "{\"testsPassed\":3,\"testsTotal\":3}", 125);
		var savedSubmission = submissionRepository.save(submission);

		assertThat(savedSubmission.getStatus()).isEqualTo(SubmissionStatus.ACCEPTED);
		assertThat(savedSubmission.getExecutionMetadata()).isEqualTo("{\"testsPassed\":3,\"testsTotal\":3}");
		assertThat(savedSubmission.getExecutionDurationMs()).isEqualTo(125);
		assertThat(savedSubmission.getUpdatedAt()).isNotNull();
	}

	@Test
	void testFindByUserAndTaskWhenHistoryExistsShouldReturnSubmissionHistory() {
		var user = userRepository.save(new User("user@example.com", "user", "hash"));
		var otherUser = userRepository.save(new User("other@example.com", "other", "hash"));
		var task = taskRepository.save(createTask("stream-flat-map-users"));
		var otherTask = taskRepository.save(createTask("stream-reduce-users"));
		submissionRepository.saveAndFlush(new Submission(user, task, "first"));
		submissionRepository.saveAndFlush(new Submission(user, task, "second"));
		submissionRepository.save(new Submission(user, otherTask, "other task"));
		submissionRepository.save(new Submission(otherUser, task, "other user"));

		var pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
		var history = submissionRepository.findByUserAndTask(user, task, pageable);
		var userHistory = submissionRepository.findByUser(user, pageable);

		assertThat(history.getTotalElements()).isEqualTo(2);
		assertThat(history.getContent())
				.extracting(Submission::getSourceCode)
				.containsExactly("second", "first");
		assertThat(userHistory.getTotalElements()).isEqualTo(3);
	}

	private Task createTask(String slug) {
		return new Task(
				"Filter active users",
				slug,
				"Use Stream API to filter active users.",
				TaskDifficulty.EASY,
				TaskTopic.STREAM_API,
				TaskStatus.PUBLISHED,
				"return users.stream();",
				"return users.stream().filter(User::active).toList();");
	}
}
