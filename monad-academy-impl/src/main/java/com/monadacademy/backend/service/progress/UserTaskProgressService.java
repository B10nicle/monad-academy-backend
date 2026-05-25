package com.monadacademy.backend.service.progress;

import org.springframework.stereotype.Service;

import com.monadacademy.backend.entity.SubmissionStatus;
import com.monadacademy.backend.entity.Task;
import com.monadacademy.backend.entity.User;
import com.monadacademy.backend.entity.UserTaskProgress;
import com.monadacademy.backend.repository.UserTaskProgressRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Updates user task progress from submission results.
 *
 * @author Monad Academy Agent
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserTaskProgressService {

	private final UserTaskProgressRepository progressRepository;

	public UserTaskProgress recordSubmission(User user, Task task, SubmissionStatus status) {
		var progress = progressRepository.findByUserAndTask(user, task)
				.orElseGet(() -> new UserTaskProgress(user, task));
		progress.recordAttempt(status);
		var savedProgress = progressRepository.save(progress);
		log.debug("Updated progress id={} userId={} taskId={} status={} attempts={}",
				savedProgress.getId(), user.getId(), task.getId(), savedProgress.getStatus(), savedProgress.getAttemptsCount());
		return savedProgress;
	}
}
