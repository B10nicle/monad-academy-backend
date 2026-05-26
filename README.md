# Monad Academy Backend

Spring Boot backend for Monad Academy, a lightweight coding practice platform inspired by LeetCode and Codewars.

## Stack

- Java 25
- Spring Boot 4
- Spring Web MVC
- Spring Security
- Spring Data JPA
- PostgreSQL
- Liquibase
- Maven Wrapper

## Local Development

Start PostgreSQL and local SMTP with Docker Compose:

```bash
docker compose up -d
```

Local verification emails are captured by Mailpit:

```text
http://localhost:8025
```

Verification links use the local Angular frontend by default:

```text
http://localhost:4200
```

Override it with `FRONTEND_BASE_URL` when needed.

Run the application:

```bash
./mvnw spring-boot:run
```

Run tests:

```bash
./mvnw test
```

Tests use Testcontainers with PostgreSQL, so Docker must be running.
