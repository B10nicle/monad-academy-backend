package com.monadacademy.backend.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

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
