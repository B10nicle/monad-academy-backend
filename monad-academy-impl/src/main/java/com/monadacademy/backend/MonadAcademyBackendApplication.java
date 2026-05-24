package com.monadacademy.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.monadacademy.backend.config.AppProperties;

/**
 * Bootstraps the Monad Academy backend application.
 *
 * @author Monad Academy Agent
 */
@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class MonadAcademyBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(MonadAcademyBackendApplication.class, args);
	}

}
