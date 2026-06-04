package com.monadacademy.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.monadacademy.backend.entity.Submission;
import com.monadacademy.backend.entity.Task;
import com.monadacademy.backend.entity.User;

/**
 * Provides persistence operations and history lookup helpers for submissions.
 *
 * @author Monad Academy Agent
 */
public interface SubmissionRepository extends JpaRepository<Submission, Long>, JpaSpecificationExecutor<Submission> {

	Page<Submission> findByUser(User user, Pageable pageable);

	Page<Submission> findByUserAndTask(User user, Task task, Pageable pageable);
}
