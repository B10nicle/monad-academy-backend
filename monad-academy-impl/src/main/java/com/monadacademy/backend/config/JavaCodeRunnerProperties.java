package com.monadacademy.backend.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds Docker sandbox settings for Java code execution.
 *
 * @author Monad Academy Agent
 */
@ConfigurationProperties(prefix = "app.java-runner")
public record JavaCodeRunnerProperties(
		String dockerImage,
		Duration compileTimeout,
		Duration runTimeout,
		String memoryLimit,
		String javaHeapLimit,
		String cpus,
		int pidsLimit,
		String tmpfsSize) {
}
