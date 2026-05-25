package com.monadacademy.backend;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Provides shared PostgreSQL Testcontainers configuration for integration tests.
 *
 * @author Monad Academy Agent
 */
public abstract class AbstractPostgresTest {

	static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine")
			.withDatabaseName("monad_academy")
			.withUsername("monad_academy")
			.withPassword("monad_academy");

	static {
		postgres.start();
	}

	@DynamicPropertySource
	static void configurePostgres(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
	}
}
