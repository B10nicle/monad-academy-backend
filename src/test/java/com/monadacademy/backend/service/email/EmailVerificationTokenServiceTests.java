package com.monadacademy.backend.service.email;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.monadacademy.backend.config.AppProperties;
import com.monadacademy.backend.entity.EmailVerificationToken;
import com.monadacademy.backend.entity.User;
import com.monadacademy.backend.exception.AppException;
import com.monadacademy.backend.exception.ErrorCode;
import com.monadacademy.backend.repository.EmailVerificationTokenRepository;
import com.monadacademy.backend.service.audit.AuditLogService;

/**
 * Verifies email verification token generation and validation rules.
 *
 * @author Monad Academy Agent
 */
@ExtendWith(MockitoExtension.class)
class EmailVerificationTokenServiceTests {

	@Mock
	AuditLogService auditLogService;

	@Mock
	EmailVerificationTokenRepository tokenRepository;

	private final AppProperties appProperties = new AppProperties(
			"http://localhost:3000",
			Duration.ofHours(24),
			new AppProperties.Jwt("test-secret-test-secret-test-secret-test-secret", Duration.ofHours(1)));

	@Test
	void testCreateTokenWhenUserProvidedShouldPersistHashedToken() {
		var service = new EmailVerificationTokenService(appProperties, auditLogService, tokenRepository);
		var user = new User("user@example.com", "b10nicle", "password-hash");
		var captor = ArgumentCaptor.forClass(EmailVerificationToken.class);

		var rawToken = service.createToken(user);

		verify(tokenRepository).save(captor.capture());
		var savedToken = captor.getValue();
		org.assertj.core.api.Assertions.assertThat(savedToken.getTokenHash()).isEqualTo(service.hash(rawToken));
		org.assertj.core.api.Assertions.assertThat(savedToken.getTokenHash()).isNotEqualTo(rawToken);
		org.assertj.core.api.Assertions.assertThat(savedToken.getExpiresAt()).isAfter(Instant.now());
	}

	@Test
	void testVerifyWhenTokenUnknownShouldThrowException() {
		var service = new EmailVerificationTokenService(appProperties, auditLogService, tokenRepository);
		when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

		var exception = org.assertj.core.api.Assertions.catchThrowableOfType(
				() -> service.verify("unknown-token"),
				AppException.class);

		org.assertj.core.api.Assertions.assertThat(exception.getCode()).isEqualTo(ErrorCode.INVALID_VERIFICATION_TOKEN);
	}

	@Test
	void testVerifyWhenTokenExpiredShouldThrowException() {
		var service = new EmailVerificationTokenService(appProperties, auditLogService, tokenRepository);
		var user = new User("user@example.com", "b10nicle", "password-hash");
		var rawToken = "expired-token";
		var token = new EmailVerificationToken(user, service.hash(rawToken), Instant.now().minusSeconds(60));
		when(tokenRepository.findByTokenHash(service.hash(rawToken))).thenReturn(Optional.of(token));

		var exception = org.assertj.core.api.Assertions.catchThrowableOfType(
				() -> service.verify(rawToken),
				AppException.class);

		org.assertj.core.api.Assertions.assertThat(exception.getCode()).isEqualTo(ErrorCode.EXPIRED_VERIFICATION_TOKEN);
	}
}
