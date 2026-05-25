package com.monadacademy.backend.service.runner;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

/**
 * Generates an executable Java wrapper around submitted solution code.
 *
 * @author Monad Academy Agent
 */
@Component
public class JavaWrapperGenerator {

	private static final String MAIN_CLASS_NAME = "Main";

	public String generate(JavaCodeRunRequest request) {
		var testCases = request.testCases().stream()
				.map(testCase -> "new TestCase(\"%s\", \"%s\")".formatted(escape(testCase.input()), escape(testCase.expectedOutput())))
				.collect(Collectors.joining(",\n\t\t\t\t"));
		return """
				import java.util.List;
				import java.util.Objects;

				public class %s {

					public static void main(String[] args) throws Exception {
						var runner = new %s();
						var testCases = List.of(
								%s);
						var passed = 0;
						for (var testCase : testCases) {
							var actual = runner.solve(testCase.input());
							if (!Objects.equals(actual, testCase.expectedOutput())) {
								System.out.println("{\\"status\\":\\"WRONG_ANSWER\\",\\"expected\\":\\"" + escapeJson(testCase.expectedOutput()) + "\\",\\"actual\\":\\"" + escapeJson(actual) + "\\"}");
								System.exit(2);
							}
							passed++;
						}
						System.out.println("{\\"status\\":\\"ACCEPTED\\",\\"testsPassed\\":" + passed + ",\\"testsTotal\\":" + testCases.size() + "}");
					}

					public String solve(String input) throws Exception {
						%s
					}

					private static String escapeJson(String value) {
						if (value == null) {
							return "";
						}
						return value.replace("\\\\", "\\\\\\\\").replace("\\"", "\\\\\\"").replace("\\n", "\\\\n").replace("\\r", "\\\\r");
					}

					record TestCase(String input, String expectedOutput) {
					}
				}
				""".formatted(MAIN_CLASS_NAME, MAIN_CLASS_NAME, testCases, request.sourceCode());
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
