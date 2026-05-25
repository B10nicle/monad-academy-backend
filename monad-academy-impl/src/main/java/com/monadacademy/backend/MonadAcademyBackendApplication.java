package com.monadacademy.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.monadacademy.backend.config.AppProperties;
import com.monadacademy.backend.config.JavaCodeRunnerProperties;

/**
 * Bootstraps the Monad Academy backend application.
 *
 * @author Monad Academy Agent
 */
@SpringBootApplication
@EnableConfigurationProperties({AppProperties.class, JavaCodeRunnerProperties.class})
public class MonadAcademyBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(MonadAcademyBackendApplication.class, args);
	}

}
