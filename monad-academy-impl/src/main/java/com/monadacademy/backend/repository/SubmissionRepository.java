package com.monadacademy.backend.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.monadacademy.backend.entity.Submission;
import com.monadacademy.backend.entity.Task;
import com.monadacademy.backend.entity.User;

/**
 * Provides persistence operations and history lookup helpers for submissions.
 *
 * @author Monad Academy Agent
 */
public interface SubmissionRepository extends JpaRepository<Submission, UUID> {

	Page<Submission> findByUser(User user, Pageable pageable);

	Page<Submission> findByUserAndTask(User user, Task task, Pageable pageable);
}
