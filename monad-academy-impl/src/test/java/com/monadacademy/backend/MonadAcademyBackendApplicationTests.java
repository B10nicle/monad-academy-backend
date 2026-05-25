package com.monadacademy.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Verifies that the Spring application context starts with PostgreSQL Testcontainers.
 *
 * @author Monad Academy Agent
 */
@SpringBootTest
@ActiveProfiles("test")
class MonadAcademyBackendApplicationTests extends AbstractPostgresTest {

	@Test
	void contextLoads() {
	}

}
