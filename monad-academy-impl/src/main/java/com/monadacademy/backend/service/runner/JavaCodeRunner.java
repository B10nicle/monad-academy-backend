package com.monadacademy.backend.service.runner;

/**
 * Executes submitted Java code against task validation data.
 *
 * @author Monad Academy Agent
 */
public interface JavaCodeRunner {

	JavaCodeRunResult run(JavaCodeRunRequest request);
}
