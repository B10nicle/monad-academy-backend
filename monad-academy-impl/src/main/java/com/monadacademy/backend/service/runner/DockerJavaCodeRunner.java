package com.monadacademy.backend.service.runner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.monadacademy.backend.config.JavaCodeRunnerProperties;
import com.monadacademy.backend.entity.SubmissionStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Compiles and executes Java submissions inside a constrained Docker sandbox.
 *
 * @author Monad Academy Agent
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DockerJavaCodeRunner implements JavaCodeRunner {

	private static final String WORKSPACE_PATH = "/workspace";
	private static final String MAIN_FILE_NAME = "Main.java";

	private final CommandExecutor commandExecutor;
	private final JavaWrapperGenerator wrapperGenerator;
	private final JavaCodeRunnerProperties properties;

	@Override
	public JavaCodeRunResult run(JavaCodeRunRequest request) {
		var startedAt = Instant.now();
		Path workDirectory = null;
		try {
			workDirectory = Files.createTempDirectory("monad-java-runner-");
			Files.writeString(workDirectory.resolve(MAIN_FILE_NAME), wrapperGenerator.generate(request), StandardCharsets.UTF_8);
			var compileResult = commandExecutor.execute(buildDockerCommand(workDirectory.toString(), List.of("javac", MAIN_FILE_NAME)), properties.compileTimeout());
			if (compileResult.timedOut()) {
				log.debug("Java submission compilation timed out");
				return result(SubmissionStatus.TIME_LIMIT_EXCEEDED, compileResult, startedAt);
			}
			if (compileResult.exitCode() != 0) {
				log.debug("Java submission compilation failed exitCode={}", compileResult.exitCode());
				return result(SubmissionStatus.COMPILATION_ERROR, compileResult, startedAt);
			}
			var runResult = commandExecutor.execute(buildDockerCommand(workDirectory.toString(), List.of("java", "-Xmx" + properties.javaHeapLimit(), "Main")), properties.runTimeout());
			if (runResult.timedOut()) {
				log.debug("Java submission execution timed out");
				return result(SubmissionStatus.TIME_LIMIT_EXCEEDED, runResult, startedAt);
			}
			var status = mapRunStatus(runResult);
			log.debug("Java submission execution completed status={} exitCode={}", status, runResult.exitCode());
			return result(status, runResult, startedAt);
		}
		catch (IOException exception) {
			log.debug("Java submission runner failed due to IO error", exception);
			return new JavaCodeRunResult(SubmissionStatus.INTERNAL_ERROR, "", exception.getMessage(), "", durationSince(startedAt));
		}
		catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			log.debug("Java submission runner was interrupted", exception);
			return new JavaCodeRunResult(SubmissionStatus.INTERNAL_ERROR, "", exception.getMessage(), "", durationSince(startedAt));
		}
		finally {
			deleteWorkspace(workDirectory);
		}
	}

	List<String> buildDockerCommand(String workDirectory, List<String> containerCommand) {
		var command = new ArrayList<>(List.of(
				"docker",
				"run",
				"--rm",
				"--network",
				"none",
				"--cap-drop",
				"ALL",
				"--security-opt",
				"no-new-privileges",
				"--read-only",
				"--memory",
				properties.memoryLimit(),
				"--cpus",
				properties.cpus(),
				"--pids-limit",
				String.valueOf(properties.pidsLimit()),
				"--tmpfs",
				"/tmp:rw,noexec,nosuid,size=" + properties.tmpfsSize(),
				"-v",
				workDirectory + ":" + WORKSPACE_PATH + ":rw",
				"-w",
				WORKSPACE_PATH,
				properties.dockerImage()));
		command.addAll(containerCommand);
		return command;
	}

	private SubmissionStatus mapRunStatus(CommandExecutionResult runResult) {
		return switch (runResult.exitCode()) {
			case 0 -> SubmissionStatus.ACCEPTED;
			case 2 -> SubmissionStatus.WRONG_ANSWER;
			default -> SubmissionStatus.RUNTIME_ERROR;
		};
	}

	private JavaCodeRunResult result(SubmissionStatus status, CommandExecutionResult commandResult, Instant startedAt) {
		return new JavaCodeRunResult(
				status,
				commandResult.output(),
				commandResult.errorOutput(),
				commandResult.output().strip(),
				durationSince(startedAt));
	}

	private long durationSince(Instant startedAt) {
		return Duration.between(startedAt, Instant.now()).toMillis();
	}

	private void deleteWorkspace(Path workDirectory) {
		if (workDirectory == null) {
			return;
		}
		try (var paths = Files.walk(workDirectory)) {
			paths.sorted((first, second) -> second.compareTo(first))
					.forEach(path -> {
						try {
							Files.deleteIfExists(path);
						}
						catch (IOException exception) {
							log.debug("Failed to delete Java runner workspace path={}", path, exception);
						}
					});
		}
		catch (IOException exception) {
			log.debug("Failed to walk Java runner workspace path={}", workDirectory, exception);
		}
	}
}
