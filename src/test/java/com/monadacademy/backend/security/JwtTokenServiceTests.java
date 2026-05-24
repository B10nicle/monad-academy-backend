package com.monadacademy.backend.security;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import com.monadacademy.backend.config.AppProperties;
import com.monadacademy.backend.entity.User;

import tools.jackson.databind.ObjectMapper;

/**
 * Verifies JWT token creation and validation.
 *
 * @author Monad Academy Agent
 */
class JwtTokenServiceTests {

	private final JwtTokenService jwtTokenService = new JwtTokenService(
			new ObjectMapper(),
			new AppProperties(
					"http://localhost:3000",
					Duration.ofHours(24),
					new AppProperties.Jwt("test-secret-test-secret-test-secret-test-secret", Duration.ofHours(1))));

	@Test
	void testParseWhenTokenIsValidShouldReturnCurrentUser() {
		var user = new User("user@example.com", "b10nicle", "password-hash");
		var token = jwtTokenService.createToken(user);

		var currentUser = jwtTokenService.parse(token);

		org.assertj.core.api.Assertions.assertThat(currentUser).isNotNull();
		org.assertj.core.api.Assertions.assertThat(currentUser.id()).isEqualTo(user.getId());
		org.assertj.core.api.Assertions.assertThat(currentUser.role()).isEqualTo(user.getRole());
	}

	@Test
	void testParseWhenTokenIsTamperedShouldReturnNull() {
		var user = new User("user@example.com", "b10nicle", "password-hash");
		var token = jwtTokenService.createToken(user);
		var tamperedToken = token.substring(0, token.length() - 2) + "aa";

		var currentUser = jwtTokenService.parse(tamperedToken);

		org.assertj.core.api.Assertions.assertThat(currentUser).isNull();
	}
}
