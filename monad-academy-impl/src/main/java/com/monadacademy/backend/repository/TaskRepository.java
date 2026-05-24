package com.monadacademy.backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.monadacademy.backend.entity.Task;

/**
 * Provides persistence operations for coding tasks.
 *
 * @author Monad Academy Agent
 */
public interface TaskRepository extends JpaRepository<Task, UUID> {

	boolean existsBySlug(String slug);

	Optional<Task> findBySlug(String slug);
}
