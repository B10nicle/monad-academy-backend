# AGENTS.md

# Monad Academy — AI Agent Instructions

This document defines rules and expectations for AI coding agents working on the Monad Academy ecosystem.

Primary backend repository:

- monad-academy-backend

Related frontend repository:

- monad-academy-frontend

This repository contains backend logic only.

---

# Project Context

Monad Academy is a lightweight educational coding platform inspired by LeetCode and Codewars.

The system consists of:

## Backend

Repository:

```text
monad-academy-backend
```

Responsibilities:

- REST API
- authentication and authorization
- business logic
- persistence
- validation
- security
- integrations
- domain rules

## Frontend

Repository:

```text
monad-academy-frontend
```

Responsibilities:

- UI
- UX
- routing
- API integration
- client-side validation
- rendering

IMPORTANT:

Do not implement frontend concerns inside backend.

Do not add UI logic, HTML rendering, client state management, or frontend build tooling into this repository.

---

# Technology Stack

Backend stack:

- Java 25
- Spring Boot 4
- Maven Wrapper
- Spring Web MVC
- Spring Security
- Spring Validation
- Spring Data JPA
- PostgreSQL
- Liquibase
- Spring Boot Actuator
- Docker Compose
- Lombok
- Springdoc OpenAPI (Swagger)

Testing stack:

- JUnit 5
- Spring Boot Test
- PostgreSQL/Testcontainers

---

# Current Backend State

Authentication foundation is implemented in `feature/auth`.

Important decisions:

- JWT tokens are issued and validated through Spring Security OAuth2 Resource Server.
- JWT signing uses Spring Security/Nimbus infrastructure with HMAC-SHA256.
- JWT configuration lives under `app.jwt`.
- Email verification stores only SHA-256 token hashes.
- Raw email verification tokens are sent only through `EmailSender`.
- Email delivery is handled by `EmailSender`. `LoggingEmailSender` is used in non-prod profiles, while `SmtpEmailSender` is used in the `prod` profile.
- JSON support uses Spring Boot Jackson 3 packages under `tools.jackson`.
- Lombok is used for constructors, getters, and JPA no-args constructors where it removes boilerplate.
- MapStruct is used for DTO mapping; mapper implementations should be generated, not handwritten.
- MapStruct Spring component model is configured through Maven compiler args.

---

# Repository Rules

Permanent branches:

```text
main
develop
```

Rules:

- Never work directly in `main`
- Always branch from `develop`
- Feature work must target `develop`

Example:

```bash
git switch develop
git pull --ff-only origin develop
git switch -c feature/<feature-name>
```

Do not rewrite branch history unless explicitly requested.

---

# Local Development

Start infrastructure:

```bash
docker compose up -d
```

Run application:

```bash
./mvnw spring-boot:run
```

Run tests:

```bash
./mvnw test
```

Run database migrations manually:

```bash
docker compose exec postgres psql -U monad_academy -d monad_academy -c "create schema if not exists monad_academy"
./mvnw -pl monad-academy-db liquibase:update
```

Always use Maven Wrapper:

```bash
./mvnw
```

Do not require globally installed Maven.

---

# Architecture Rules

Follow layered architecture:

```text
controller -> service -> repository
```

Rules:

## Controllers

Controllers should:

- expose REST endpoints
- validate input
- map HTTP requests/responses
- delegate to services

Controllers must NOT:

- contain business logic
- directly access repositories
- contain persistence logic

## Services

Services contain:

- business logic
- orchestration
- domain rules
- transactional boundaries

## Repositories

Repositories are persistence-only.

Repositories must NOT contain:

- business rules
- orchestration
- HTTP concerns

Prefer Spring Data JPA patterns.

---

# Module Structure

The backend is a Maven multi-module project.

Current module structure:

```text
monad-academy-backend
├── monad-academy-api
├── monad-academy-db
├── monad-academy-domain
└── monad-academy-impl
```

Module responsibilities:

- `monad-academy-api`: DTOs and API-facing contracts
- `monad-academy-db`: Liquibase master changelog and migration scripts
- `monad-academy-domain`: domain model and JPA entities
- `monad-academy-impl`: Spring Boot application, controllers, services, repositories, security, configuration, and tests

Do not put frontend concerns or UI resources into any backend module.

Do not add `static` or `templates` resource directories unless the backend explicitly starts rendering server-side HTML.

## Package Structure

Preferred structure:

```text
com.monadacademy.backend
├── config
├── controller
├── dto
├── entity
├── exception
├── mapper
├── repository
├── security
├── service
└── util
```

Do not introduce new architectural layers unless justified.

Avoid overengineering.

Service implementations must be grouped by domain-oriented subpackages when the package grows.

Current service package structure:

```text
service
├── audit
├── auth
├── email
└── user
```

---

# Coding Style

Prefer:

- constructor injection
- immutable DTOs where practical
- meaningful naming
- small focused methods
- explicit service APIs
- composition over complexity

Allowed:

- Lombok where useful

Required:

- use Lombok for constructor injection boilerplate
- use Lombok for simple getters in entities
- use Lombok for protected JPA no-args constructors
- use Lombok `@Slf4j` for class logging
- use MapStruct for DTO/entity mapping
- add class-level Javadoc to every Java type
- include `@author Monad Academy Agent` in class-level Javadoc for agent-created Java types

Avoid:

- field injection
- magic strings
- giant classes
- unrelated refactoring
- premature abstractions
- unnecessary design patterns

Prefer readability over cleverness.

## Annotation Ordering

Annotations above classes and methods must be ordered by length (shorter to longer).

Rule:

```text
shorter annotation first
longer annotation after
```

Example:

Good:

```java
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class UserServiceTest {
}
```

Bad:

```java
@ActiveProfiles("test")
@Testcontainers
@SpringBootTest
class UserServiceTest {
}
```

Apply this rule consistently for:

- classes
- methods
- test classes
- configuration classes

Do not reorder annotations unless the resulting order follows the length rule.

## Java Type Documentation

Every Java type must have class-level Javadoc before annotations or declarations.

Required format:

```java
/**
 * Briefly describes what the type does.
 *
 * @author Monad Academy Agent
 */
```

Keep descriptions short and factual.

---

# Database Rules

Database is PostgreSQL.

Schema changes MUST use Liquibase.

Master changelog:

```text
monad-academy-db/src/main/resources/db/changelog/db.changelog-master.xml
```

Rules:

- never silently edit existing migrations
- create new changesets
- keep changesets in separate XML files under `monad-academy-db/src/main/resources/db/changelog/changes`
- include new changeset files from the master changelog
- name changeset files with a zero-padded numeric prefix, for example `01-create-users.xml`
- create and use the application schema `monad_academy`
- do not create application tables in PostgreSQL `public`
- specify `schemaName="monad_academy"` for schema objects in Liquibase changes
- keep JPA entities synchronized with migrations
- prefer PostgreSQL-compatible behavior
- avoid H2-specific assumptions

If database structure changes:

1. add Liquibase migration
2. update entity
3. update tests

---

# Security Rules

Security changes are high impact.

Be conservative.

Do:

- keep SecurityConfig focused
- use Spring Security OAuth2 Resource Server for bearer JWT validation
- protect endpoints intentionally
- validate authentication flows
- respect least privilege

Do NOT:

- disable security to make tests pass
- add custom JWT servlet filters while Resource Server can handle bearer tokens
- hardcode secrets
- expose actuator broadly
- weaken authentication casually

Never commit:

```text
.env
*.pem
*.key
secrets/
```

---

# API Rules

API documentation is automatically generated using Swagger (Springdoc OpenAPI). Review the UI to understand existing contracts.

This backend serves the frontend repository:

```text
monad-academy-frontend
```

When changing APIs:

- consider frontend compatibility
- avoid breaking contracts unnecessarily
- update DTOs intentionally
- document behavior changes

Prefer stable response contracts.

Avoid random field renaming.

---

# Testing Rules

Behavior changes require tests.

Run:

```bash
./mvnw test
```

before considering work complete.

Expected testing:

## Service logic

Unit tests.

## Persistence

Repository/integration tests.

## Controllers

Web/API tests where meaningful.

## Security

Security tests when auth changes.

## Java Runner Sandbox

The Docker-backed Java runner must keep the execution container hardened:

- disable networking with `--network none`
- run as a non-root user
- keep the root filesystem read-only
- mount runtime workspace as read-only
- enforce memory, memory-swap, CPU, PID, and tmpfs limits
- drop Linux capabilities and prevent privilege escalation
- use named containers and explicit cleanup for timeout/error paths

Tests should be:

- deterministic
- isolated
- readable
- minimal but sufficient

Test assertions should:

- use AssertJ static imports such as `assertThat`, `assertThatThrownBy`, and `catchThrowableOfType`
- avoid fully qualified calls such as `org.assertj.core.api.Assertions.assertThat(...)`

Avoid flaky tests.

---

# Dependency Rules

Do not add dependencies casually.

Before introducing a new dependency ask:

1. Can Spring already solve this?
2. Can existing project code solve this?
3. Is the dependency justified?

Avoid dependency bloat.

---

# Frontend / Backend Boundary

Frontend repository:

```text
monad-academy-frontend
```

Backend repository:

```text
monad-academy-backend
```

AI agents must respect boundaries.

Backend SHOULD:

- expose APIs
- validate input
- return DTOs
- enforce authorization
- implement business logic

Backend SHOULD NOT:

- implement UI logic
- include frontend libraries
- generate HTML
- manage browser concerns

If a requested feature belongs to frontend:

Explain what backend API changes are needed instead of implementing frontend behavior here.

---

# Pull Request Expectations

PRs should include:

- summary of changes
- tests performed
- migration notes if database changed
- security implications if relevant

Prefer small PRs.

Avoid large mixed refactors.

---

# Agent Workflow

Before coding:

1. Read README.md
2. Read pom.xml
3. Inspect existing implementation
4. Preserve style consistency
5. Minimize change scope

When implementing:

1. Reuse existing patterns
2. Prefer smallest viable change
3. Avoid speculative abstractions
4. Add tests if behavior changes

Before finishing:

1. Run tests

```bash
./mvnw test
```

2. Check imports
3. Check Liquibase consistency
4. Ensure API compatibility
5. Verify no secrets are committed
6. Update `tasks/backend-feature-roadmap.md` to mark completed feature work and add implementation notes

---

# What NOT To Do

Do NOT:

- push directly to main
- rewrite migrations casually
- introduce architecture astronauts patterns
- disable security to fix tests
- refactor unrelated code
- add frameworks without justification
- mix frontend code into backend
- commit credentials
- invent unnecessary abstractions

---

# Decision Making

When uncertain:

Prefer:

```text
simple
predictable
readable
consistent
```

Avoid:

```text
clever
overengineered
speculative
premature abstraction
```

The codebase should stay maintainable for long-term development.
---

# Product Roadmap

The backend feature roadmap and detailed specifications are located in the `tasks/` directory:

- Roadmap: `tasks/backend-feature-roadmap.md`
- Feature Specs: `tasks/feature/*.md`

Agents should refer to these documents to understand the current goal and implementation details of each feature.

When a feature is completed, agents must update `tasks/backend-feature-roadmap.md` in the same PR:

- set the feature status to completed
- update the progress overview row
- add concise notes about the PR, important technical decisions, migrations, and security-relevant behavior
