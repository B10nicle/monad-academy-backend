package com.monadacademy.backend.dto;

import com.monadacademy.backend.entity.TaskDifficulty;
import com.monadacademy.backend.entity.TaskTopic;

/**
 * Represents published task summary data for public browsing.
 *
 * @author Monad Academy Agent
 */
public record PublicTaskSummaryResponse(
		Long id,
		String title,
		String slug,
		TaskDifficulty difficulty,
		TaskTopic topic) {
}
