package com.monadacademy.backend.dto;

import java.util.List;

/**
 * Represents a stable paginated API response.
 *
 * @author Monad Academy Agent
 */
public record PageResponse<T>(
		List<T> content,
		int page,
		int size,
		long totalElements,
		int totalPages) {
}
