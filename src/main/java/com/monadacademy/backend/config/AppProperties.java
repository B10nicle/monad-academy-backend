package com.monadacademy.backend.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds application-level configuration properties for auth and frontend integration.
 *
 * @author Monad Academy Agent
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(
		String frontendBaseUrl,
		Duration emailVerificationTokenTtl,
		Jwt jwt) {

	public record Jwt(
			String secret,
			Duration expiration) {
	}
}
