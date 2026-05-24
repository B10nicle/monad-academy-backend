package com.monadacademy.backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.monadacademy.backend.entity.EmailVerificationToken;
import com.monadacademy.backend.entity.User;
import com.monadacademy.backend.repository.AuditLogRepository;
import com.monadacademy.backend.repository.EmailVerificationTokenRepository;
import com.monadacademy.backend.repository.UserRepository;
import com.monadacademy.backend.service.email.EmailVerificationTokenService;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * Verifies authentication API flows with PostgreSQL Testcontainers.
 *
 * @author Monad Academy Agent
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTests {

	@Container
	static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine")
			.withDatabaseName("monad_academy")
			.withUsername("monad_academy")
			.withPassword("monad_academy");

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	UserRepository userRepository;

	@Autowired
	AuditLogRepository auditLogRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	EmailVerificationTokenService tokenService;

	@Autowired
	EmailVerificationTokenRepository tokenRepository;

	@DynamicPropertySource
	static void configurePostgres(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
	}

	@BeforeEach
	void setUp() {
		auditLogRepository.deleteAll();
		tokenRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	void testRegisterWhenRequestIsValidShouldCreatePendingUser() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "user@example.com",
								  "username": "b10nicle",
								  "password": "password"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Registration completed. Check your email to verify your account"));

		var user = userRepository.findByEmailIgnoreCase("user@example.com").orElseThrow();
		org.assertj.core.api.Assertions.assertThat(user.getStatus().name()).isEqualTo("PENDING_EMAIL_VERIFICATION");
		org.assertj.core.api.Assertions.assertThat(tokenRepository.findAll()).hasSize(1);
	}

	@Test
	void testRegisterWhenEmailAlreadyExistsShouldThrowException() throws Exception {
		createPendingUser("user@example.com", "b10nicle");

		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "USER@example.com",
								  "username": "other",
								  "password": "password"
								}
								"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"));
	}

	@Test
	void testRegisterWhenUsernameAlreadyExistsShouldThrowException() throws Exception {
		createPendingUser("user@example.com", "b10nicle");

		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "other@example.com",
								  "username": "B10nicle",
								  "password": "password"
								}
								"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("USERNAME_ALREADY_EXISTS"));
	}

	@Test
	void testVerifyEmailWhenTokenIsValidShouldActivateUser() throws Exception {
		var user = createPendingUser("user@example.com", "b10nicle");
		var token = tokenService.createToken(user);

		mockMvc.perform(post("/api/auth/verify-email")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "token": "%s"
								}
								""".formatted(token)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Email verified"));

		var activatedUser = userRepository.findById(user.getId()).orElseThrow();
		org.assertj.core.api.Assertions.assertThat(activatedUser.getStatus().name()).isEqualTo("ACTIVE");
		org.assertj.core.api.Assertions.assertThat(activatedUser.getEmailVerifiedAt()).isNotNull();
	}

	@Test
	void testVerifyEmailWhenTokenExpiredShouldThrowException() throws Exception {
		var user = createPendingUser("user@example.com", "b10nicle");
		var rawToken = "expired-token";
		tokenRepository.save(new EmailVerificationToken(user, tokenService.hash(rawToken), Instant.now().minusSeconds(60)));

		mockMvc.perform(post("/api/auth/verify-email")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "token": "%s"
								}
								""".formatted(rawToken)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("EXPIRED_VERIFICATION_TOKEN"));
	}

	@Test
	void testResendVerificationWhenEmailUnknownShouldReturnNeutralMessage() throws Exception {
		mockMvc.perform(post("/api/auth/resend-verification")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "unknown@example.com"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("If the email exists, verification instructions have been sent"));
	}

	@Test
	void testLoginWhenUserIsActiveShouldReturnToken() throws Exception {
		createActiveUser("user@example.com", "b10nicle");

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "login": "b10nicle",
								  "password": "password"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isString());
	}

	@Test
	void testLoginWhenUserIsUnverifiedShouldThrowException() throws Exception {
		createPendingUser("user@example.com", "b10nicle");

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "login": "user@example.com",
								  "password": "password"
								}
								"""))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("EMAIL_NOT_VERIFIED"));
	}

	@Test
	void testLoginWhenPasswordWrongShouldThrowException() throws Exception {
		createActiveUser("user@example.com", "b10nicle");

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "login": "user@example.com",
								  "password": "wrong-password"
								}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
	}

	@Test
	void testMeWhenJwtIsValidShouldReturnCurrentUser() throws Exception {
		createActiveUser("user@example.com", "b10nicle");
		var loginResult = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "login": "user@example.com",
								  "password": "password"
								}
								"""))
				.andExpect(status().isOk())
				.andReturn();
		var response = objectMapper.readValue(
				loginResult.getResponse().getContentAsByteArray(),
				new TypeReference<Map<String, String>>() {
				});

		mockMvc.perform(get("/api/users/me")
						.header("Authorization", "Bearer " + response.get("token")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("user@example.com"))
				.andExpect(jsonPath("$.username").value("b10nicle"))
				.andExpect(jsonPath("$.role").value("USER"))
				.andExpect(jsonPath("$.status").value("ACTIVE"));
	}

	private User createPendingUser(String email, String username) {
		return userRepository.save(new User(email, username, passwordEncoder.encode("password")));
	}

	private User createActiveUser(String email, String username) {
		var user = new User(email, username, passwordEncoder.encode("password"));
		user.activate();
		return userRepository.save(user);
	}
}
