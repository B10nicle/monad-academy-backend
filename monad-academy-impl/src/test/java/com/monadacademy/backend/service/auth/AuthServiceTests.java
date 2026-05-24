package com.monadacademy.backend.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.monadacademy.backend.config.AppProperties;
import com.monadacademy.backend.dto.LoginRequest;
import com.monadacademy.backend.dto.RegisterRequest;
import com.monadacademy.backend.entity.AuditEventType;
import com.monadacademy.backend.entity.User;
import com.monadacademy.backend.exception.AppException;
import com.monadacademy.backend.exception.ErrorCode;
import com.monadacademy.backend.repository.UserRepository;
import com.monadacademy.backend.security.JwtTokenService;
import com.monadacademy.backend.service.audit.AuditLogService;
import com.monadacademy.backend.service.email.EmailSender;
import com.monadacademy.backend.service.email.EmailVerificationTokenService;

/**
 * Verifies authentication service business rules.
 *
 * @author Monad Academy Agent
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTests {

	@Mock
	EmailSender emailSender;

	@Mock
	AuditLogService auditLogService;

	@Mock
	JwtTokenService jwtTokenService;

	@Mock
	PasswordEncoder passwordEncoder;

	@Mock
	UserRepository userRepository;

	@Mock
	EmailVerificationTokenService tokenService;

	private final AppProperties appProperties = new AppProperties(
			"http://localhost:3000",
			Duration.ofHours(24),
			new AppProperties.Jwt("test-secret-test-secret-test-secret-test-secret", Duration.ofHours(1)));

	@Test
	void testRegisterWhenEmailAlreadyExistsShouldThrowException() {
		var authService = authService();
		var request = new RegisterRequest("user@example.com", "b10nicle", "password");
		when(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(true);

		var exception = catchThrowableOfType(
				() -> authService.register(request),
				AppException.class);

		assertThat(exception.getCode()).isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS);
	}

	@Test
	void testLoginWhenPasswordWrongShouldThrowException() {
		var authService = authService();
		var user = new User("user@example.com", "b10nicle", "password-hash");
		user.activate();
		when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("wrong-password", "password-hash")).thenReturn(false);

		var exception = catchThrowableOfType(
				() -> authService.login(new LoginRequest("user@example.com", "wrong-password")),
				AppException.class);

		assertThat(exception.getCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS);
		verify(auditLogService).log(user, AuditEventType.USER_LOGIN_FAILED, null);
	}

	private AuthService authService() {
		return new AuthService(
				emailSender,
				appProperties,
				auditLogService,
				jwtTokenService,
				passwordEncoder,
				userRepository,
				tokenService);
	}
}
