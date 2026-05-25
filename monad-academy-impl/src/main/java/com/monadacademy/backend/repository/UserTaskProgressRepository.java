package com.monadacademy.backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.monadacademy.backend.entity.Task;
import com.monadacademy.backend.entity.User;
import com.monadacademy.backend.entity.UserTaskProgress;
import com.monadacademy.backend.entity.UserTaskProgressStatus;

/**
 * Provides persistence operations and lookup helpers for user task progress.
 *
 * @author Monad Academy Agent
 */
public interface UserTaskProgressRepository extends JpaRepository<UserTaskProgress, UUID> {

	long countByUserAndStatus(User user, UserTaskProgressStatus status);

	Page<UserTaskProgress> findByUser(User user, Pageable pageable);

	Optional<UserTaskProgress> findByUserAndTask(User user, Task task);
}
