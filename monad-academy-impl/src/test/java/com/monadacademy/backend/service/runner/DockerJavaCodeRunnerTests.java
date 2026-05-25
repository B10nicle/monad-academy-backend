package com.monadacademy.backend.service.runner;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.monadacademy.backend.config.JavaCodeRunnerProperties;
import com.monadacademy.backend.entity.SubmissionStatus;

/**
 * Verifies Docker Java runner command hardening and result mapping.
 *
 * @author Monad Academy Agent
 */
class DockerJavaCodeRunnerTests {

	private final JavaCodeRunnerProperties properties = new JavaCodeRunnerProperties(
			"eclipse-temurin:25-jdk-alpine",
			Duration.ofSeconds(10),
			Duration.ofSeconds(3),
			Duration.ofSeconds(2),
			"128m",
			"128m",
			"64m",
			"0.5",
			64,
			"64m",
			"65534:65534");

	@Test
	void testBuildDockerCommandWhenCalledShouldApplySandboxLimits() {
		var runner = new DockerJavaCodeRunner(new FakeCommandExecutor(List.of()), new JavaWrapperGenerator(), properties);

		var command = runner.buildDockerCommand("/tmp/work", List.of("java", "-Xmx64m", "Main"), "runner-test", false);

		assertThat(command).containsSequence("docker", "run", "--rm");
		assertThat(command).containsSequence("--name", "runner-test");
		assertThat(command).containsSequence("--network", "none");
		assertThat(command).containsSequence("--user", "65534:65534");
		assertThat(command).containsSequence("--cap-drop", "ALL");
		assertThat(command).containsSequence("--security-opt", "no-new-privileges");
		assertThat(command).contains("--read-only");
		assertThat(command).containsSequence("--memory", "128m");
		assertThat(command).containsSequence("--memory-swap", "128m");
		assertThat(command).containsSequence("--cpus", "0.5");
		assertThat(command).containsSequence("--pids-limit", "64");
		assertThat(command).containsSequence("--tmpfs", "/tmp:rw,noexec,nosuid,size=64m");
		assertThat(command).containsSequence("--mount", "type=bind,source=/tmp/work,target=/workspace,readonly");
		assertThat(command).containsSequence("-w", "/workspace");
		assertThat(command).contains("eclipse-temurin:25-jdk-alpine");
	}

	@Test
	void testBuildDockerCommandWhenCompilingShouldUseWritableWorkspace() {
		var runner = new DockerJavaCodeRunner(new FakeCommandExecutor(List.of()), new JavaWrapperGenerator(), properties);

		var command = runner.buildDockerCommand("/tmp/work", List.of("javac", "Main.java"), "runner-test", true);

		assertThat(command).containsSequence("--mount", "type=bind,source=/tmp/work,target=/workspace");
		assertThat(command).doesNotContain("type=bind,source=/tmp/work,target=/workspace,readonly");
	}

	@Test
	void testRunWhenCompilationFailsShouldReturnCompilationError() {
		var executor = new FakeCommandExecutor(List.of(
				new CommandExecutionResult(1, false, "", "Main.java: error", 12)));
		var runner = new DockerJavaCodeRunner(executor, new JavaWrapperGenerator(), properties);

		var result = runner.run(request());

		assertThat(result.status()).isEqualTo(SubmissionStatus.COMPILATION_ERROR);
		assertThat(executor.commands()).hasSize(1);
	}

	@Test
	void testRunWhenExecutionTimesOutShouldReturnTimeLimitExceeded() {
		var executor = new FakeCommandExecutor(List.of(
				new CommandExecutionResult(0, false, "", "", 12),
				new CommandExecutionResult(-1, true, "", "", 3000),
				new CommandExecutionResult(0, false, "", "", 20)));
		var runner = new DockerJavaCodeRunner(executor, new JavaWrapperGenerator(), properties);

		var result = runner.run(request());

		assertThat(result.status()).isEqualTo(SubmissionStatus.TIME_LIMIT_EXCEEDED);
		assertThat(executor.commands()).hasSize(3);
		assertThat(executor.commands().get(2)).containsSequence("docker", "rm", "-f");
	}

	@Test
	void testRunWhenWrongAnswerExitCodeReturnedShouldReturnWrongAnswer() {
		var executor = new FakeCommandExecutor(List.of(
				new CommandExecutionResult(0, false, "", "", 12),
				new CommandExecutionResult(2, false, "{\"status\":\"WRONG_ANSWER\"}", "", 30)));
		var runner = new DockerJavaCodeRunner(executor, new JavaWrapperGenerator(), properties);

		var result = runner.run(request());

		assertThat(result.status()).isEqualTo(SubmissionStatus.WRONG_ANSWER);
		assertThat(result.executionMetadata()).isEqualTo("{\"status\":\"WRONG_ANSWER\"}");
	}

	private JavaCodeRunRequest request() {
		return new JavaCodeRunRequest(
				"return input;",
				List.of(new JavaCodeRunnerTestCase("value", "value")));
	}

	private static class FakeCommandExecutor implements CommandExecutor {

		private int index;
		private final List<List<String>> commands = new ArrayList<>();
		private final List<CommandExecutionResult> results;

		FakeCommandExecutor(List<CommandExecutionResult> results) {
			this.results = results;
		}

		@Override
		public CommandExecutionResult execute(List<String> command, Duration timeout) throws IOException {
			commands.add(command);
			return results.get(index++);
		}

		List<List<String>> commands() {
			return commands;
		}
	}
}
