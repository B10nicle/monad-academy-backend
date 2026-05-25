package com.monadacademy.backend.entity;

/**
 * Defines execution lifecycle and result states for user submissions.
 *
 * @author Monad Academy Agent
 */
public enum SubmissionStatus {
	PENDING,
	RUNNING,
	ACCEPTED,
	WRONG_ANSWER,
	COMPILATION_ERROR,
	RUNTIME_ERROR,
	TIME_LIMIT_EXCEEDED,
	INTERNAL_ERROR
}
