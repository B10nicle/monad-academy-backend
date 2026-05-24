package com.monadacademy.backend.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.monadacademy.backend.config.AppProperties;
import com.monadacademy.backend.entity.AuditEventType;
import com.monadacademy.backend.entity.EmailVerificationToken;
import com.monadacademy.backend.entity.User;
import com.monadacademy.backend.exception.AppException;
import com.monadacademy.backend.exception.ErrorCode;
import com.monadacademy.backend.repository.EmailVerificationTokenRepository;

@Service
public class EmailVerificationTokenService {

	private final SecureRandom secureRandom = new SecureRandom();
	private final AppProperties appProperties;
	private final AuditLogService auditLogService;
	private final EmailVerificationTokenRepository tokenRepository;

	public EmailVerificationTokenService(
			AppProperties appProperties,
			AuditLogService auditLogService,
			EmailVerificationTokenRepository tokenRepository) {
		this.appProperties = appProperties;
		this.auditLogService = auditLogService;
		this.tokenRepository = tokenRepository;
	}

	@Transactional
	public String createToken(User user) {
		var rawToken = generateRawToken();
		var expiresAt = Instant.now().plus(appProperties.emailVerificationTokenTtl());
		var token = new EmailVerificationToken(user, hash(rawToken), expiresAt);
		tokenRepository.save(token);
		return rawToken;
	}

	@Transactional
	public User verify(String rawToken) {
		var token = tokenRepository.findByTokenHash(hash(rawToken))
				.orElseThrow(() -> new AppException(ErrorCode.INVALID_VERIFICATION_TOKEN, HttpStatus.BAD_REQUEST));

		if (token.isConfirmed()) {
			throw new AppException(ErrorCode.INVALID_VERIFICATION_TOKEN, HttpStatus.BAD_REQUEST);
		}

		if (token.isExpired()) {
			throw new AppException(ErrorCode.EXPIRED_VERIFICATION_TOKEN, HttpStatus.BAD_REQUEST);
		}

		var user = token.getUser();
		user.activate();
		token.confirm();
		auditLogService.log(user, AuditEventType.USER_EMAIL_VERIFIED, null);
		return user;
	}

	public String hash(String rawToken) {
		try {
			var digest = MessageDigest.getInstance("SHA-256");
			var hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
			return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
		}
		catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException(exception);
		}
	}

	private String generateRawToken() {
		var bytes = new byte[32];
		secureRandom.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}
}
