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
| feature/admin-tasks-api | [ ] | Not started |
| feature/public-tasks-api | [ ] | Not started |
| feature/submissions-domain | [ ] | Not started |
| feature/java-code-runner | [ ] | Not started |
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

Status: [ ] Not started

Goal:

Admin management for tasks.

Scope:

- create task
- update task
- publish task
- archive task
- manage test cases

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

Status: [ ] Not started

Goal:

Public task browsing.

Scope:

- list tasks
- get task details

Endpoints:

- `GET /api/tasks`
- `GET /api/tasks/{slug}`

Dependencies:

- feature/tasks-domain

---

## 5. feature/submissions-domain

Status: [ ] Not started

Goal:

Submission persistence and lifecycle.

Scope:

- `Submission`
- `SubmissionStatus`
- submission history
- execution metadata

Dependencies:

- feature/tasks-domain
- feature/auth

---

## 6. feature/java-code-runner

Status: [ ] Not started

Goal:

Secure Java execution environment.

Scope:

- generate Java wrapper class
- compile code
- execute in isolated environment
- timeout handling
- memory limits
- sandbox execution

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
3. feature/admin-tasks-api
4. feature/public-tasks-api
5. feature/submissions-domain
6. feature/java-code-runner
7. feature/submission-api
8. feature/progress
9. feature/admin-submissions
10. feature/docker-sandbox-hardening
