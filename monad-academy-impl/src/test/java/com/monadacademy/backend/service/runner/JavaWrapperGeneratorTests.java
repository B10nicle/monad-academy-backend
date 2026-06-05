package com.monadacademy.backend.service.runner;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.tools.ToolProvider;

import org.junit.jupiter.api.Test;

/**
 * Verifies Java wrapper source generation for submitted code.
 *
 * @author Monad Academy Agent
 */
class JavaWrapperGeneratorTests {

	private final JavaWrapperGenerator generator = new JavaWrapperGenerator();

	@Test
	void testGenerateWhenRequestProvidedShouldWrapSourceAndEscapedTestCases() {
		var source = generator.generate(new JavaCodeRunRequest(
				"""
						class Solution {
						    public String upper(String input) {
						        return input.toUpperCase();
						    }
						}
						""",
				"upper",
				List.of(new JavaCodeRunnerTestCase("\"a\\\"b\"", "A\"B"))));

		assertThat(source).contains("public class Main");
		assertThat(source).contains("class Solution");
		assertThat(source).contains("solution.upper(\"a\\\"b\")");
		assertThat(source).contains("return input.toUpperCase();");
		assertThat(source).contains("var expected0 = \"A\\\"B\"");
	}

	@Test
	void testGenerateWhenValidSourceProvidedShouldCompileWrapper() throws Exception {
		var source = generator.generate(new JavaCodeRunRequest(
				"""
						class Solution {
						    public String upper(String input) {
						        return input.toUpperCase();
						    }
						}
						""",
				"upper",
				List.of(new JavaCodeRunnerTestCase("\"abc\"", "ABC"))));
		var workDirectory = Files.createTempDirectory("wrapper-generator-test-");
		var sourceFile = workDirectory.resolve("Main.java");
		try {
			Files.writeString(sourceFile, source);

			var compiler = ToolProvider.getSystemJavaCompiler();
			var exitCode = compiler.run(null, null, null, sourceFile.toString());

			assertThat(exitCode).isZero();
		}
		finally {
			delete(workDirectory);
		}
	}

	@Test
	void testGenerateWhenArrayInputProvidedShouldCompileWrapper() throws Exception {
		var source = generator.generate(new JavaCodeRunRequest(
				"""
						class Solution {
						    public String developerNamesBySalary(String[] developers) {
						        return java.util.Arrays.stream(developers)
						                .map(value -> value.split(":"))
						                .filter(parts -> "Dev".equals(parts[1]))
						                .filter(parts -> Integer.parseInt(parts[2]) > 6000)
						                .map(parts -> parts[0])
						                .sorted()
						                .collect(java.util.stream.Collectors.joining(","));
						    }
						}
						""",
				"developerNamesBySalary",
				List.of(new JavaCodeRunnerTestCase(
						"new String[]{\"Zoe:Dev:7000\", \"Adam:Dev:8000\", \"John:QA:9000\"}",
						"Adam,Zoe"))));
		var workDirectory = Files.createTempDirectory("wrapper-generator-test-");
		var sourceFile = workDirectory.resolve("Main.java");
		try {
			Files.writeString(sourceFile, source);

			var compiler = ToolProvider.getSystemJavaCompiler();
			var exitCode = compiler.run(null, null, null, sourceFile.toString());

			assertThat(exitCode).isZero();
		}
		finally {
			delete(workDirectory);
		}
	}

	private void delete(Path workDirectory) throws Exception {
		try (var paths = Files.walk(workDirectory)) {
			for (var path : paths.sorted((first, second) -> second.compareTo(first)).toList()) {
				Files.deleteIfExists(path);
			}
		}
	}
}
