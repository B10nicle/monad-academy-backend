# Monad Academy Backend Feature Roadmap

This file tracks backend feature implementation progress.

Legend:

- [x] completed
- [~] in progress
- [ ] not started

---

## Progress Overview

| Feature | Status | Notes |
|---|---:|---|
| feature/auth | [x] | Implemented in PR #5 |
| feature/tasks-domain | [x] | Implemented in feature/tasks-domain |
| feature/admin-tasks-api | [x] | Implemented in feature/admin-tasks-api |
| feature/public-tasks-api | [x] | Implemented in feature/public-tasks-api |
| feature/submissions-domain | [x] | Implemented in feature/submissions-domain |
| feature/java-code-runner | [x] | Implemented in feature/java-code-runner |
| feature/submission-api | [ ] | Not started |
| feature/progress | [ ] | Not started |
| feature/admin-submissions | [ ] | Not started |
| feature/docker-sandbox-hardening | [ ] | Not started |

---

# Features

## 1. feature/auth

Status: [x] Completed

Goal:

Authentication and authorization foundation.

Delivered:

- registration
- email verification
- resend verification email
- login
- JWT authentication
- `/api/users/me`
- audit logging
- consistent error handling
- automated tests

Notes:

- implemented in PR #5
- JWT issuing and validation are implemented through Spring Security OAuth2 Resource Server
- backend was split into Maven modules: api, db, domain, and impl

---

## 2. feature/tasks-domain

Status: [x] Completed

Goal:

Implement domain model for coding tasks.

Scope:

- `Task`
- `TaskDifficulty`
- `TaskTopic`
- `TaskStatus`
- `TaskTestCase`

Expected deliverables:

- entities
- Liquibase migrations
- repositories
- DTOs
- validations

Delivered:

- `Task` and `TaskTestCase` entities
- `TaskDifficulty`, `TaskTopic`, and `TaskStatus` enums
- task request/response DTOs with validation
- Liquibase migrations for `tasks` and `task_test_cases`
- task repositories
- PostgreSQL Testcontainers repository tests

Notes:

- implemented in feature/tasks-domain
- task schema changes are stored in `04-create-tasks.xml` and `05-create-task-test-cases.xml`

Dependencies:

- feature/auth

---

## 3. feature/admin-tasks-api

Status: [x] Completed

Goal:

Admin management for tasks.

Scope:

- create task
- update task
- publish task
- archive task
- manage test cases

Delivered:

- admin-only task management endpoints
- task create and update flows
- publish and archive lifecycle endpoints
- test case creation endpoint
- task management audit events
- JWT role-to-authority mapping for ADMIN access
- PostgreSQL Testcontainers controller tests

Endpoints:

- `POST /api/admin/tasks`
- `PUT /api/admin/tasks/{id}`
- `POST /api/admin/tasks/{id}/publish`
- `POST /api/admin/tasks/{id}/archive`
- `POST /api/admin/tasks/{id}/test-cases`

Dependencies:

- feature/tasks-domain

---

## 4. feature/public-tasks-api

Status: [x] Completed

Goal:

Public task browsing.

Scope:

- list tasks
- get task details

Delivered:

- anonymous published task listing endpoint
- anonymous published task details endpoint
- stable pagination response DTO
- public task DTOs that hide solutions and hidden tests
- public task access tests with PostgreSQL Testcontainers

Endpoints:

- `GET /api/tasks`
- `GET /api/tasks/{slug}`

Dependencies:

- feature/tasks-domain

---

## 5. feature/submissions-domain

Status: [x] Completed

Goal:

Submission persistence and lifecycle.

Scope:

- `Submission`
- `SubmissionStatus`
- submission history
- execution metadata

Delivered:

- `Submission` entity with source code, status, execution metadata, and execution duration
- `SubmissionStatus` lifecycle/result enum
- Liquibase migration for `submissions`
- submission repository history queries
- PostgreSQL Testcontainers repository tests

Dependencies:

- feature/tasks-domain
- feature/auth

---

## 6. feature/java-code-runner

Status: [x] Completed

Goal:

Secure Java execution environment.

Scope:

- generate Java wrapper class
- compile code
- execute in isolated environment
- timeout handling
- memory limits
- sandbox execution

Delivered:

- Java wrapper generator for submitted solution code
- Docker-backed Java code runner service
- compile and run timeout handling
- memory, CPU, PID, network, filesystem, capability, and privilege sandbox flags
- execution result mapping to `SubmissionStatus`
- runner unit tests for wrapper compilation, sandbox command flags, timeout handling, and result mapping

Dependencies:

- feature/submissions-domain

---

## 7. feature/submission-api

Status: [ ] Not started

Goal:

User submission flow.

Endpoints:

- `POST /api/submissions`
- `GET /api/submissions/my`
- `GET /api/tasks/{id}/submissions/my`

Dependencies:

- feature/submissions-domain
- feature/java-code-runner

---

## 8. feature/progress

Status: [ ] Not started

Goal:

Track user progress.

Scope:

- `UserTaskProgress`
- attempts count
- solved state
- progress aggregation

Dependencies:

- feature/submission-api

---

## 9. feature/admin-submissions

Status: [ ] Not started

Goal:

Admin access to user submissions.

Endpoints:

- `GET /api/admin/submissions`
- `GET /api/admin/users/{id}/submissions`

Dependencies:

- feature/submission-api

---

## 10. feature/docker-sandbox-hardening

Status: [ ] Not started

Goal:

Production hardening of execution sandbox.

Scope:

- network disabled
- filesystem isolation
- resource limits
- container cleanup
- security tuning

Dependencies:

- feature/java-code-runner

---

## Recommended Order

1. feature/auth [x]
2. feature/tasks-domain [x]
3. feature/admin-tasks-api [x]
4. feature/public-tasks-api [x]
5. feature/submissions-domain [x]
6. feature/java-code-runner [x]
7. feature/submission-api
8. feature/progress
9. feature/admin-submissions
10. feature/docker-sandbox-hardening
