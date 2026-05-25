package com.monadacademy.backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.monadacademy.backend.entity.Task;
import com.monadacademy.backend.entity.TaskStatus;

/**
 * Provides persistence operations for coding tasks.
 *
 * @author Monad Academy Agent
 */
public interface TaskRepository extends JpaRepository<Task, UUID> {

	boolean existsBySlug(String slug);

	Optional<Task> findBySlug(String slug);

	Optional<Task> findByIdAndStatus(UUID id, TaskStatus status);

	Page<Task> findByStatus(TaskStatus status, Pageable pageable);

	Optional<Task> findBySlugAndStatus(String slug, TaskStatus status);
}
