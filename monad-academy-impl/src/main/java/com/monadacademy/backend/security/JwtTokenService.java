package com.monadacademy.backend.security;

import java.time.Instant;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.monadacademy.backend.config.AppProperties;
import com.monadacademy.backend.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Creates JWT access tokens through Spring Security OAuth2 infrastructure.
 *
 * @author Monad Academy Agent
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtTokenService {

	private final JwtEncoder jwtEncoder;
	private final AppProperties appProperties;

	public String createToken(User user) {
		var now = Instant.now();
		var claims = JwtClaimsSet.builder()
				.subject(user.getId().toString())
				.issuedAt(now)
				.expiresAt(now.plus(appProperties.jwt().expiration()))
				.claim("role", user.getRole().name())
				.build();
		var headers = JwsHeader.with(MacAlgorithm.HS256).build();
		var token = jwtEncoder.encode(JwtEncoderParameters.from(headers, claims)).getTokenValue();
		log.debug("Created access token for userId={} role={}", user.getId(), user.getRole());
		return token;
	}
}
