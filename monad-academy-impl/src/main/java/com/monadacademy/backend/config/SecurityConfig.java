package com.monadacademy.backend.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import com.monadacademy.backend.entity.UserRole;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

/**
 * Configures stateless API security with OAuth2 Resource Server JWT support.
 *
 * @author Monad Academy Agent
 */
@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.csrf(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(
								"/api/auth/register",
								"/api/auth/login",
								"/api/auth/verify-email",
								"/api/auth/resend-verification",
								"/actuator/health",
								"/actuator/info",
								"/v3/api-docs/**",
								"/swagger-ui/**",
								"/swagger-ui.html").permitAll()
						.requestMatchers("/api/admin/**").hasRole(UserRole.ADMIN.name())
						.anyRequest().authenticated())
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
				.build();
	}

	@Bean
	JwtAuthenticationConverter jwtAuthenticationConverter() {
		var converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(jwt -> {
			var role = jwt.getClaimAsString("role");
			if (role == null) {
				return java.util.List.of();
			}
			return java.util.List.of(new SimpleGrantedAuthority("ROLE_" + role));
		});
		return converter;
	}

	@Bean
	JwtEncoder jwtEncoder(AppProperties appProperties) {
		return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecret(appProperties)));
	}

	@Bean
	JwtDecoder jwtDecoder(AppProperties appProperties) {
		return NimbusJwtDecoder.withSecretKey(jwtSecret(appProperties))
				.macAlgorithm(MacAlgorithm.HS256)
				.build();
	}

	private SecretKeySpec jwtSecret(AppProperties appProperties) {
		return new SecretKeySpec(appProperties.jwt().secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
	}
}
