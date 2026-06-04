package com.monadacademy.backend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.util.ReflectionTestUtils;

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
					"http://localhost:4200",
					Duration.ofHours(24),
					new AppProperties.Jwt(JWT_SECRET, Duration.ofHours(1))));

	@Test
	void testCreateTokenWhenTokenIsValidShouldCreateDecodableJwt() {
		var user = user();
		var token = jwtTokenService.createToken(user);

		var jwt = jwtDecoder.decode(token);

		assertThat(jwt.getSubject()).isEqualTo(user.getId().toString());
		assertThat(jwt.getClaimAsString("role")).isEqualTo(user.getRole().name());
	}

	@Test
	void testDecodeWhenTokenIsTamperedShouldThrowException() {
		var user = user();
		var token = jwtTokenService.createToken(user);
		var tamperedToken = token.substring(0, token.length() - 2) + "aa";

		assertThatThrownBy(() -> jwtDecoder.decode(tamperedToken))
				.isInstanceOf(JwtException.class);
	}

	private SecretKeySpec jwtSecret() {
		return new SecretKeySpec(JWT_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
	}

	private User user() {
		var user = new User("user@example.com", "b10nicle", "password-hash");
		ReflectionTestUtils.setField(user, "id", 1L);
		return user;
	}
}
