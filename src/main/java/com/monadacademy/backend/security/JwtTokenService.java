package com.monadacademy.backend.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

import com.monadacademy.backend.config.AppProperties;
import com.monadacademy.backend.entity.User;
import com.monadacademy.backend.entity.UserRole;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
public class JwtTokenService {

	private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
	private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
	private static final String ALGORITHM = "HmacSHA256";

	private final ObjectMapper objectMapper;
	private final AppProperties appProperties;

	public JwtTokenService(ObjectMapper objectMapper, AppProperties appProperties) {
		this.objectMapper = objectMapper;
		this.appProperties = appProperties;
	}

	public String createToken(User user) {
		var now = Instant.now();
		var expiresAt = now.plus(appProperties.jwt().expiration());
		var header = Map.of("alg", "HS256", "typ", "JWT");
		var payload = Map.of(
				"sub", user.getId().toString(),
				"role", user.getRole().name(),
				"iat", now.getEpochSecond(),
				"exp", expiresAt.getEpochSecond());
		var encodedHeader = encodeJson(header);
		var encodedPayload = encodeJson(payload);
		var content = encodedHeader + "." + encodedPayload;
		return content + "." + sign(content);
	}

	public CurrentUser parse(String token) {
		var parts = token.split("\\.");
		if (parts.length != 3) {
			return null;
		}

		var content = parts[0] + "." + parts[1];
		if (!MessageDigestSupport.matches(sign(content), parts[2])) {
			return null;
		}

		try {
			var payload = objectMapper.readValue(DECODER.decode(parts[1]), new TypeReference<Map<String, Object>>() {
			});
			var expiresAt = ((Number) payload.get("exp")).longValue();
			if (Instant.now().getEpochSecond() >= expiresAt) {
				return null;
			}
			var userId = UUID.fromString((String) payload.get("sub"));
			var role = UserRole.valueOf((String) payload.get("role"));
			return new CurrentUser(userId, role);
		}
		catch (RuntimeException exception) {
			return null;
		}
	}

	private String encodeJson(Map<String, ?> value) {
		try {
			return ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
		}
		catch (RuntimeException exception) {
			throw new IllegalStateException(exception);
		}
	}

	private String sign(String content) {
		try {
			var mac = Mac.getInstance(ALGORITHM);
			var key = new SecretKeySpec(appProperties.jwt().secret().getBytes(StandardCharsets.UTF_8), ALGORITHM);
			mac.init(key);
			return ENCODER.encodeToString(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
		}
		catch (Exception exception) {
			throw new IllegalStateException(exception);
		}
	}
}
