package com.monadacademy.backend.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Provides constant-time string comparison for signatures.
 *
 * @author Monad Academy Agent
 */
final class MessageDigestSupport {

	private MessageDigestSupport() {
	}

	static boolean matches(String expected, String actual) {
		return MessageDigest.isEqual(
				expected.getBytes(StandardCharsets.UTF_8),
				actual.getBytes(StandardCharsets.UTF_8));
	}
}
