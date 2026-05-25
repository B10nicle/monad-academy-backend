package com.monadacademy.backend.service.runner;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

/**
 * Executes external commands with a bounded timeout.
 *
 * @author Monad Academy Agent
 */
interface CommandExecutor {

	CommandExecutionResult execute(List<String> command, Duration timeout) throws IOException, InterruptedException;
}
