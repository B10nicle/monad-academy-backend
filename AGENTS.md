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

Testing stack:

- JUnit 5
- Spring Boot Test
- PostgreSQL/Testcontainers

---

# Current Backend State

Authentication foundation is implemented in `feature/auth`.

Important decisions:

- JWT tokens are signed with HMAC-SHA256 using JDK crypto APIs.
- No dedicated JWT dependency is used unless future requirements justify it.
- JWT configuration lives under `app.jwt`.
- Email verification stores only SHA-256 token hashes.
- Raw email verification tokens are sent only through `EmailSender`.
- The default email sender is `LoggingEmailSender` for local development.
- JSON support uses Spring Boot Jackson 3 packages under `tools.jackson`.
- Lombok is used for constructors, getters, and JPA no-args constructors where it removes boilerplate.

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

# Package Structure

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
src/main/resources/db/changelog/db.changelog-master.yaml
```

Rules:

- never silently edit existing migrations
- create new changesets
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
- protect endpoints intentionally
- validate authentication flows
- respect least privilege

Do NOT:

- disable security to make tests pass
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

Tests should be:

- deterministic
- isolated
- readable
- minimal but sufficient

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
