package com.monadacademy.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.monadacademy.backend.entity.Task;
import com.monadacademy.backend.entity.TaskTestCase;

/**
 * Provides persistence operations for task validation test cases.
 *
 * @author Monad Academy Agent
 */
public interface TaskTestCaseRepository extends JpaRepository<TaskTestCase, Long> {

	List<TaskTestCase> findByTaskOrderByOrderIndexAsc(Task task);

	List<TaskTestCase> findByTaskAndHiddenFalseOrderByOrderIndexAsc(Task task);
}
