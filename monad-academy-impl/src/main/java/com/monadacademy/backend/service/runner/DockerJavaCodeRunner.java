package com.monadacademy.backend.service.runner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

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
	private static final String CONTAINER_NAME_PREFIX = "monad-java-runner-";
	private static final EnumSet<PosixFilePermission> WORKSPACE_PERMISSIONS = EnumSet.of(
			PosixFilePermission.OWNER_READ,
			PosixFilePermission.OWNER_WRITE,
			PosixFilePermission.OWNER_EXECUTE,
			PosixFilePermission.GROUP_READ,
			PosixFilePermission.GROUP_WRITE,
			PosixFilePermission.GROUP_EXECUTE,
			PosixFilePermission.OTHERS_READ,
			PosixFilePermission.OTHERS_WRITE,
			PosixFilePermission.OTHERS_EXECUTE);
	private static final EnumSet<PosixFilePermission> SOURCE_FILE_PERMISSIONS = EnumSet.of(
			PosixFilePermission.OWNER_READ,
			PosixFilePermission.OWNER_WRITE,
			PosixFilePermission.GROUP_READ,
			PosixFilePermission.OTHERS_READ);

	private final CommandExecutor commandExecutor;
	private final JavaWrapperGenerator wrapperGenerator;
	private final JavaCodeRunnerProperties properties;

	@Override
	public JavaCodeRunResult run(JavaCodeRunRequest request) {
		var startedAt = Instant.now();
		Path workDirectory = null;
		try {
			workDirectory = Files.createTempDirectory("monad-java-runner-");
			var mainFile = workDirectory.resolve(MAIN_FILE_NAME);
			Files.writeString(mainFile, wrapperGenerator.generate(request), StandardCharsets.UTF_8);
			prepareWorkspacePermissions(workDirectory, mainFile);
			var compileResult = executeContainer(
					buildDockerCommand(workDirectory.toString(), List.of("javac", MAIN_FILE_NAME), containerName("compile"), true),
					properties.compileTimeout());
			if (compileResult.timedOut()) {
				log.debug("Java submission compilation timed out");
				return result(SubmissionStatus.TIME_LIMIT_EXCEEDED, compileResult, startedAt);
			}
			if (compileResult.exitCode() != 0) {
				log.debug("Java submission compilation failed exitCode={}", compileResult.exitCode());
				return result(SubmissionStatus.COMPILATION_ERROR, compileResult, startedAt);
			}
			var runResult = executeContainer(
					buildDockerCommand(workDirectory.toString(), List.of("java", "-Xmx" + properties.javaHeapLimit(), "Main"), containerName("run"), false),
					properties.runTimeout());
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

	List<String> buildDockerCommand(String workDirectory, List<String> containerCommand, String containerName, boolean writableWorkspace) {
		var command = new ArrayList<>(List.of(
				"docker",
				"run",
				"--rm",
				"--name",
				containerName,
				"--network",
				"none",
				"--user",
				properties.containerUser(),
				"--cap-drop",
				"ALL",
				"--security-opt",
				"no-new-privileges",
				"--read-only",
				"--memory",
				properties.memoryLimit(),
				"--memory-swap",
				properties.memorySwapLimit(),
				"--cpus",
				properties.cpus(),
				"--pids-limit",
				String.valueOf(properties.pidsLimit()),
				"--tmpfs",
				"/tmp:rw,noexec,nosuid,size=" + properties.tmpfsSize(),
				"--mount",
				workspaceMount(workDirectory, writableWorkspace),
				"-w",
				WORKSPACE_PATH,
				properties.dockerImage()));
		command.addAll(containerCommand);
		return command;
	}

	private CommandExecutionResult executeContainer(List<String> command, Duration timeout) throws IOException, InterruptedException {
		var containerName = dockerContainerName(command);
		try {
			var result = commandExecutor.execute(command, timeout);
			if (result.timedOut()) {
				cleanupContainer(containerName);
			}
			return result;
		}
		catch (IOException | InterruptedException exception) {
			cleanupContainer(containerName);
			throw exception;
		}
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

	private String containerName(String phase) {
		return CONTAINER_NAME_PREFIX + phase + "-" + UUID.randomUUID();
	}

	private String dockerContainerName(List<String> command) {
		var nameFlagIndex = command.indexOf("--name");
		if (nameFlagIndex < 0 || nameFlagIndex + 1 >= command.size()) {
			return "";
		}
		return command.get(nameFlagIndex + 1);
	}

	private void prepareWorkspacePermissions(Path workDirectory, Path mainFile) {
		try {
			Files.setPosixFilePermissions(workDirectory, WORKSPACE_PERMISSIONS);
			Files.setPosixFilePermissions(mainFile, SOURCE_FILE_PERMISSIONS);
		}
		catch (UnsupportedOperationException exception) {
			log.debug("POSIX permissions are not supported for Java runner workspace path={}", workDirectory);
		}
		catch (IOException exception) {
			log.debug("Failed to update Java runner workspace permissions path={}", workDirectory, exception);
		}
	}

	private String workspaceMount(String workDirectory, boolean writableWorkspace) {
		var mount = "type=bind,source=%s,target=%s".formatted(workDirectory, WORKSPACE_PATH);
		if (!writableWorkspace) {
			return mount + ",readonly";
		}
		return mount;
	}

	private void cleanupContainer(String containerName) {
		if (containerName.isBlank()) {
			return;
		}
		try {
			commandExecutor.execute(List.of("docker", "rm", "-f", containerName), properties.cleanupTimeout());
			log.debug("Cleaned Java runner container name={}", containerName);
		}
		catch (IOException exception) {
			log.debug("Failed to clean Java runner container name={}", containerName, exception);
		}
		catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			log.debug("Java runner container cleanup interrupted name={}", containerName, exception);
		}
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
