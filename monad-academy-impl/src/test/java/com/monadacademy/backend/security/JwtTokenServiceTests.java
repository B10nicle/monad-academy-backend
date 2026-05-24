package com.monadacademy.backend.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.monadacademy.backend.config.AppProperties;
import com.monadacademy.backend.entity.User;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

/**
 * Verifies JWT token creation through Spring Security OAuth2 infrastructure.
 *
 * @author Monad Academy Agent
 */
class JwtTokenServiceTests {

	private static final String JWT_SECRET = "test-secret-test-secret-test-secret-test-secret";

	private final JwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(jwtSecret())
			.macAlgorithm(MacAlgorithm.HS256)
			.build();
	private final JwtTokenService jwtTokenService = new JwtTokenService(
			new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecret())),
			new AppProperties(
					"http://localhost:3000",
					Duration.ofHours(24),
					new AppProperties.Jwt(JWT_SECRET, Duration.ofHours(1))));

	@Test
	void testCreateTokenWhenTokenIsValidShouldCreateDecodableJwt() {
		var user = new User("user@example.com", "b10nicle", "password-hash");
		var token = jwtTokenService.createToken(user);

		var jwt = jwtDecoder.decode(token);

		org.assertj.core.api.Assertions.assertThat(jwt.getSubject()).isEqualTo(user.getId().toString());
		org.assertj.core.api.Assertions.assertThat(jwt.getClaimAsString("role")).isEqualTo(user.getRole().name());
	}

	@Test
	void testDecodeWhenTokenIsTamperedShouldThrowException() {
		var user = new User("user@example.com", "b10nicle", "password-hash");
		var token = jwtTokenService.createToken(user);
		var tamperedToken = token.substring(0, token.length() - 2) + "aa";

		org.assertj.core.api.Assertions.assertThatThrownBy(() -> jwtDecoder.decode(tamperedToken))
				.isInstanceOf(JwtException.class);
	}

	private SecretKeySpec jwtSecret() {
		return new SecretKeySpec(JWT_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
	}
}
