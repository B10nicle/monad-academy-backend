package com.monadacademy.backend.service.auth;

import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.monadacademy.backend.config.AppProperties;
import com.monadacademy.backend.dto.AuthResponse;
import com.monadacademy.backend.dto.LoginRequest;
import com.monadacademy.backend.dto.MessageResponse;
import com.monadacademy.backend.dto.RegisterRequest;
import com.monadacademy.backend.dto.ResendVerificationRequest;
import com.monadacademy.backend.dto.VerifyEmailRequest;
import com.monadacademy.backend.entity.AuditEventType;
import com.monadacademy.backend.entity.User;
import com.monadacademy.backend.entity.UserStatus;
import com.monadacademy.backend.exception.AppException;
import com.monadacademy.backend.exception.ErrorCode;
import com.monadacademy.backend.repository.UserRepository;
import com.monadacademy.backend.security.JwtTokenService;
import com.monadacademy.backend.service.audit.AuditLogService;
import com.monadacademy.backend.service.email.EmailSender;
import com.monadacademy.backend.service.email.EmailVerificationTokenService;

import lombok.RequiredArgsConstructor;

/**
 * Orchestrates registration, verification, resend, and login flows.
 *
 * @author Monad Academy Agent
 */
@Service
@RequiredArgsConstructor
public class AuthService {

	private static final String VERIFICATION_SENT_MESSAGE = "If the email exists, verification instructions have been sent";
	private static final String REGISTRATION_MESSAGE = "Registration completed. Check your email to verify your account";
	private static final String EMAIL_VERIFIED_MESSAGE = "Email verified";

	private final EmailSender emailSender;
	private final AppProperties appProperties;
	private final AuditLogService auditLogService;
	private final JwtTokenService jwtTokenService;
	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepository;
	private final EmailVerificationTokenService tokenService;

	@Transactional
	public MessageResponse register(RegisterRequest request) {
		var email = normalizeEmail(request.email());
		var username = request.username().trim();

		if (userRepository.existsByEmailIgnoreCase(email)) {
			throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS, HttpStatus.CONFLICT);
		}

		if (userRepository.existsByUsernameIgnoreCase(username)) {
			throw new AppException(ErrorCode.USERNAME_ALREADY_EXISTS, HttpStatus.CONFLICT);
		}

		var user = new User(email, username, passwordEncoder.encode(request.password()));
		userRepository.save(user);
		var token = tokenService.createToken(user);
		emailSender.sendVerificationEmail(user.getEmail(), verificationLink(token));
		auditLogService.log(user, AuditEventType.USER_REGISTERED, null);
		return new MessageResponse(REGISTRATION_MESSAGE);
	}

	@Transactional
	public MessageResponse verifyEmail(VerifyEmailRequest request) {
		tokenService.verify(request.token());
		return new MessageResponse(EMAIL_VERIFIED_MESSAGE);
	}

	@Transactional
	public MessageResponse resendVerification(ResendVerificationRequest request) {
		var email = normalizeEmail(request.email());
		userRepository.findByEmailIgnoreCase(email)
				.filter(user -> user.getStatus() != UserStatus.ACTIVE)
				.ifPresent(user -> {
					var token = tokenService.createToken(user);
					emailSender.sendVerificationEmail(user.getEmail(), verificationLink(token));
					auditLogService.log(user, AuditEventType.USER_VERIFICATION_EMAIL_RESENT, null);
				});
		return new MessageResponse(VERIFICATION_SENT_MESSAGE);
	}

	@Transactional
	public AuthResponse login(LoginRequest request) {
		var login = request.login().trim();
		var user = findByLogin(login);
		if (user == null) {
			auditLogService.log(null, AuditEventType.USER_LOGIN_FAILED, "login=" + login);
			throw new AppException(ErrorCode.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED);
		}

		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			auditLogService.log(user, AuditEventType.USER_LOGIN_FAILED, null);
			throw new AppException(ErrorCode.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED);
		}

		validateLoginStatus(user);
		user.updateLastLoginAt();
		auditLogService.log(user, AuditEventType.USER_LOGIN_SUCCEEDED, null);
		return new AuthResponse(jwtTokenService.createToken(user));
	}

	private User findByLogin(String login) {
		return userRepository.findByEmailIgnoreCase(login)
				.or(() -> userRepository.findByUsernameIgnoreCase(login))
				.orElse(null);
	}

	private void validateLoginStatus(User user) {
		if (user.getStatus() == UserStatus.PENDING_EMAIL_VERIFICATION) {
			auditLogService.log(user, AuditEventType.USER_LOGIN_FAILED, null);
			throw new AppException(ErrorCode.EMAIL_NOT_VERIFIED, HttpStatus.FORBIDDEN);
		}
		if (user.getStatus() == UserStatus.BLOCKED) {
			auditLogService.log(user, AuditEventType.USER_LOGIN_FAILED, null);
			throw new AppException(ErrorCode.USER_BLOCKED, HttpStatus.FORBIDDEN);
		}
		if (user.getStatus() == UserStatus.DELETED) {
			auditLogService.log(user, AuditEventType.USER_LOGIN_FAILED, null);
			throw new AppException(ErrorCode.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED);
		}
	}

	private String verificationLink(String token) {
		return appProperties.frontendBaseUrl() + "/verify-email?token=" + token;
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
