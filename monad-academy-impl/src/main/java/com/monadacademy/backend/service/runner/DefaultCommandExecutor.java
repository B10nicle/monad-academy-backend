package com.monadacademy.backend.service.runner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

/**
 * Executes operating system processes and captures standard streams.
 *
 * @author Monad Academy Agent
 */
@Component
class DefaultCommandExecutor implements CommandExecutor {

	@Override
	public CommandExecutionResult execute(List<String> command, Duration timeout) throws IOException, InterruptedException {
		var startedAt = Instant.now();
		var process = new ProcessBuilder(command).start();
		var output = CompletableFuture.supplyAsync(() -> read(process.getInputStream()));
		var errorOutput = CompletableFuture.supplyAsync(() -> read(process.getErrorStream()));
		var completed = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
		if (!completed) {
			process.destroyForcibly();
			process.waitFor();
			return new CommandExecutionResult(-1, true, output.join(), errorOutput.join(), durationSince(startedAt));
		}
		return new CommandExecutionResult(process.exitValue(), false, output.join(), errorOutput.join(), durationSince(startedAt));
	}

	private long durationSince(Instant startedAt) {
		return Duration.between(startedAt, Instant.now()).toMillis();
	}

	private String read(java.io.InputStream inputStream) {
		try (inputStream) {
			return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
		}
		catch (IOException exception) {
			return exception.getMessage();
		}
	}
}
