package com.monadacademy.backend.service.task;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Verifies Java Solution method signature extraction.
 *
 * @author Monad Academy Agent
 */
class TaskMethodSignatureResolverTests {

	private final TaskMethodSignatureResolver resolver = new TaskMethodSignatureResolver();

	@Test
	void testResolveShouldExtractSolutionMethodSignature() {
		var signature = resolver.resolve("""
				class Solution {
				    public int totalWaviness(int num1, int num2) {
				        return 0;
				    }
				}
				""");

		assertThat(signature).hasValue(new TaskMethodSignature("totalWaviness", "int", "int num1, int num2"));
	}

	@Test
	void testResolveShouldIgnoreThrowsDeclaration() {
		var signature = resolver.resolve("""
				class Solution {
				    public String developerNamesBySalary(String[] developers) throws Exception {
				        return "";
				    }
				}
				""");

		assertThat(signature).hasValue(new TaskMethodSignature(
				"developerNamesBySalary",
				"String",
				"String[] developers"));
	}

	@Test
	void testResolveShouldReturnEmptyWhenSolutionMethodIsMissing() {
		assertThat(resolver.resolve("class Solution {}")).isEmpty();
	}
}
