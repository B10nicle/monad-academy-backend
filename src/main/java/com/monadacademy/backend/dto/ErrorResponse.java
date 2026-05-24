package com.monadacademy.backend.dto;

public record ErrorResponse(
		String code,
		String message) {
}
