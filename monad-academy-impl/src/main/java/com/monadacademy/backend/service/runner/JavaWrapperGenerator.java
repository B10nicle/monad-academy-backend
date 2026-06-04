package com.monadacademy.backend.service.runner;

import org.springframework.stereotype.Component;

/**
 * Generates an executable Java wrapper around submitted solution code.
 *
 * @author Monad Academy Agent
 */
@Component
public class JavaWrapperGenerator {

	private static final String MAIN_CLASS_NAME = "Main";
	private static final String SOLUTION_CLASS_NAME = "Solution";

	public String generate(JavaCodeRunRequest request) {
		var testCases = testCases(request);
		return """
				import java.math.*;
				import java.util.*;
				import java.util.stream.*;
				import java.util.Arrays;
				import java.util.Objects;

				public class %s {

					public static void main(String[] args) throws Exception {
						var solution = new %s();
						var passed = 0;
				%s
						System.out.println("{\\"status\\":\\"ACCEPTED\\",\\"testsPassed\\":" + passed + ",\\"testsTotal\\":%d}");
					}

					private static String normalizeResult(Object value) {
						if (value == null) {
							return "null";
						}
						if (!value.getClass().isArray()) {
							return String.valueOf(value);
						}
						if (value instanceof Object[] array) {
							return Arrays.deepToString(array);
						}
						if (value instanceof int[] array) {
							return Arrays.toString(array);
						}
						if (value instanceof long[] array) {
							return Arrays.toString(array);
						}
						if (value instanceof double[] array) {
							return Arrays.toString(array);
						}
						if (value instanceof boolean[] array) {
							return Arrays.toString(array);
						}
						if (value instanceof char[] array) {
							return Arrays.toString(array);
						}
						if (value instanceof byte[] array) {
							return Arrays.toString(array);
						}
						if (value instanceof short[] array) {
							return Arrays.toString(array);
						}
						if (value instanceof float[] array) {
							return Arrays.toString(array);
						}
						return String.valueOf(value);
					}

					private static String escapeJson(String value) {
						if (value == null) {
							return "";
						}
						return value.replace("\\\\", "\\\\\\\\").replace("\\"", "\\\\\\"").replace("\\n", "\\\\n").replace("\\r", "\\\\r");
					}
				}

				%s
				""".formatted(MAIN_CLASS_NAME, SOLUTION_CLASS_NAME, testCases, request.testCases().size(), request.sourceCode());
	}

	private String testCases(JavaCodeRunRequest request) {
		var builder = new StringBuilder();
		for (var index = 0; index < request.testCases().size(); index++) {
			var testCase = request.testCases().get(index);
			builder.append("""
							var actual%d = normalizeResult(solution.%s(%s));
							var expected%d = "%s";
							if (!Objects.equals(actual%d, expected%d)) {
								System.out.println("{\\"status\\":\\"WRONG_ANSWER\\",\\"expected\\":\\"" + escapeJson(expected%d) + "\\",\\"actual\\":\\"" + escapeJson(actual%d) + "\\"}");
								System.exit(2);
							}
							passed++;
					""".formatted(
					index,
					request.methodName(),
					testCase.input(),
					index,
					escape(testCase.expectedOutput()),
					index,
					index,
					index,
					index));
		}
		return builder.toString();
	}

	private String escape(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\n", "\\n")
				.replace("\r", "\\r")
				.replace("\t", "\\t");
	}
}
