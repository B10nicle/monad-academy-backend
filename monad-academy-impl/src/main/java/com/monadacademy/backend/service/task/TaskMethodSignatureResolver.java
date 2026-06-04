package com.monadacademy.backend.service.task;

import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

/**
 * Extracts task method metadata from Java Solution source code.
 *
 * @author Monad Academy Agent
 */
@Component
public class TaskMethodSignatureResolver {

	private static final Pattern SOLUTION_METHOD_PATTERN = Pattern.compile(
			"(?s)(?:public|protected|private)?\\s*(?:static\\s+)?([\\w.$<>?,\\[\\]\\s]+?)\\s+([A-Za-z_$][A-Za-z0-9_$]*)\\s*\\(([^)]*)\\)\\s*\\{");

	public Optional<TaskMethodSignature> resolve(String sourceCode) {
		if (sourceCode == null || sourceCode.isBlank()) {
			return Optional.empty();
		}

		var matcher = SOLUTION_METHOD_PATTERN.matcher(sourceCode);
		while (matcher.find()) {
			var returnType = normalizeReturnType(matcher.group(1));
			var methodName = matcher.group(2).trim();
			var parameters = normalizeWhitespace(matcher.group(3));
			if (!"class".equals(returnType) && !"Solution".equals(methodName)) {
				return Optional.of(new TaskMethodSignature(methodName, returnType, parameters));
			}
		}
		return Optional.empty();
	}

	private String normalizeWhitespace(String value) {
		return value.trim().replaceAll("\\s+", " ");
	}

	private String normalizeReturnType(String value) {
		return normalizeWhitespace(value)
				.replaceFirst("^(public|protected|private)\\s+", "")
				.replaceFirst("^static\\s+", "");
	}
}
