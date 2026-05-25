package com.monadacademy.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import com.monadacademy.backend.AbstractPostgresTest;
import com.monadacademy.backend.entity.SubmissionStatus;
import com.monadacademy.backend.entity.Task;
import com.monadacademy.backend.entity.TaskDifficulty;
import com.monadacademy.backend.entity.TaskStatus;
import com.monadacademy.backend.entity.TaskTopic;
import com.monadacademy.backend.entity.User;
import com.monadacademy.backend.entity.UserTaskProgress;
import com.monadacademy.backend.entity.UserTaskProgressStatus;

/**
 * Verifies user task progress persistence and aggregation queries.
 *
 * @author Monad Academy Agent
 */
@SpringBootTest
@ActiveProfiles("test")
class UserTaskProgressRepositoryTests extends AbstractPostgresTest {

	@Autowired
	UserRepository userRepository;

	@Autowired
	TaskRepository taskRepository;

	@Autowired
	UserTaskProgressRepository progressRepository;

	@BeforeEach
	void setUp() {
		progressRepository.deleteAll();
		taskRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	void testRecordAttemptWhenWrongAnswerShouldTrackInProgressAttempt() {
		var user = userRepository.save(new User("user@example.com", "user", "hash"));
		var task = taskRepository.save(createTask("stream-filter-values"));
		var progress = new UserTaskProgress(user, task);

		progress.recordAttempt(SubmissionStatus.WRONG_ANSWER);
		var savedProgress = progressRepository.save(progress);

		assertThat(savedProgress.getStatus()).isEqualTo(UserTaskProgressStatus.IN_PROGRESS);
		assertThat(savedProgress.getAttemptsCount()).isEqualTo(1);
		assertThat(savedProgress.getSolvedAt()).isNull();
	}

	@Test
	void testRecordAttemptWhenAcceptedShouldTrackSolvedStateOnce() {
		var user = userRepository.save(new User("user@example.com", "user", "hash"));
		var task = taskRepository.save(createTask("stream-map-values"));
		var progress = new UserTaskProgress(user, task);

		progress.recordAttempt(SubmissionStatus.ACCEPTED);
		var firstSolvedAt = progress.getSolvedAt();
		progress.recordAttempt(SubmissionStatus.ACCEPTED);
		var savedProgress = progressRepository.save(progress);

		assertThat(savedProgress.getStatus()).isEqualTo(UserTaskProgressStatus.SOLVED);
		assertThat(savedProgress.getAttemptsCount()).isEqualTo(2);
		assertThat(savedProgress.getSolvedAt()).isEqualTo(firstSolvedAt);
	}

	@Test
	void testFindByUserAndTaskWhenProgressExistsShouldReturnProgressAndSolvedCount() {
		var user = userRepository.save(new User("user@example.com", "user", "hash"));
		var task = taskRepository.save(createTask("stream-reduce-values"));
		var progress = new UserTaskProgress(user, task);
		progress.recordAttempt(SubmissionStatus.ACCEPTED);
		progressRepository.save(progress);

		var foundProgress = progressRepository.findByUserAndTask(user, task).orElseThrow();
		var userProgressPage = progressRepository.findByUser(user, PageRequest.of(0, 10));

		assertThat(foundProgress.getStatus()).isEqualTo(UserTaskProgressStatus.SOLVED);
		assertThat(userProgressPage.getTotalElements()).isEqualTo(1);
		assertThat(progressRepository.countByUserAndStatus(user, UserTaskProgressStatus.SOLVED)).isEqualTo(1);
	}

	private Task createTask(String slug) {
		return new Task(
				"Map values",
				slug,
				"Use Stream API to map values.",
				TaskDifficulty.EASY,
				TaskTopic.STREAM_API,
				TaskStatus.PUBLISHED,
				"return values.stream();",
				"return values.stream().map(String::toUpperCase).toList();");
	}
}
