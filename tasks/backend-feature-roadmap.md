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
| feature/email-sender-implementation | [x] | Implemented SMTP email delivery |
| feature/tasks-domain | [x] | Implemented in feature/tasks-domain |
| feature/admin-tasks-api | [x] | Implemented in feature/admin-tasks-api |
| feature/public-tasks-api | [x] | Implemented in feature/public-tasks-api |
| feature/submissions-domain | [x] | Implemented in feature/submissions-domain |
| feature/java-code-runner | [x] | Implemented in feature/java-code-runner |
| feature/submission-api | [x] | Implemented in feature/submission-api |
| feature/progress | [x] | Implemented in feature/progress |
| feature/admin-submissions | [x] | Implemented in feature/admin-submissions |
| feature/docker-sandbox-hardening | [x] | Implemented in feature/docker-sandbox-hardening |
| feature/entity-long-ids | [x] | Converted entity identifiers from UUID to Long |
| feature/solution-class-submissions | [x] | Added LeetCode-style Solution class submissions |
| feature/frontend-response-formatting | [x] | Formatted frontend-facing timestamps and durations |
| feature/admin-task-create-fields | [x] | Derived task method metadata during admin task authoring |
| feature/developers-by-salary-signature | [x] | Updated developers-by-salary to array input and descriptive method name |

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

Status: [x] Completed

Goal:

User submission flow.

Endpoints:

- `POST /api/submissions`
- `GET /api/submissions/my`
- `GET /api/tasks/{id}/submissions/my`

Delivered:

- authenticated submission creation endpoint
- authenticated current-user submission history endpoint
- authenticated current-user task submission history endpoint
- submission execution through Java code runner
- persisted submission result metadata

Dependencies:

- feature/submissions-domain
- feature/java-code-runner

---

## 8. feature/progress

Status: [x] Completed

Goal:

Track user progress.

Scope:

- `UserTaskProgress`
- attempts count
- solved state
- progress aggregation

Delivered:

- `UserTaskProgress` entity and status enum
- Liquibase migration for `user_task_progress`
- progress repository lookup and solved-count aggregation
- automatic progress updates after submission execution
- PostgreSQL Testcontainers repository and API flow tests

Dependencies:

- feature/submission-api

---

## 9. feature/admin-submissions

Status: [x] Completed

Goal:

Admin access to user submissions.

Endpoints:

- `GET /api/admin/submissions`
- `GET /api/admin/users/{id}/submissions`

Delivered:

- admin-only submission listing endpoint
- admin-only user submission history endpoint
- optional filtering by user, task, and submission status
- normalized pagination for admin submission history
- PostgreSQL Testcontainers controller tests

Dependencies:

- feature/submission-api

---

## 10. feature/docker-sandbox-hardening

Status: [x] Completed

Goal:

Production hardening of execution sandbox.

Scope:

- network disabled
- filesystem isolation
- resource limits
- container cleanup
- security tuning

Delivered:

- named Docker containers for explicit cleanup after timeouts and startup failures
- non-root container user for compilation and execution
- memory swap limit aligned with container memory limit
- read-only workspace mount during execution
- writable compile workspace with explicit host permissions for non-root `javac`
- runner unit tests for hardened Docker command flags and timeout cleanup

Dependencies:

- feature/java-code-runner

---

## 11. feature/entity-long-ids

Status: [x] Completed

Goal:

Use database-generated numeric identifiers for persisted entities and API contracts.

Delivered:

- converted JPA entity ids from `UUID` to generated `Long`
- converted API DTO ids, request ids, controller path variables, service parameters, and repository id types to `Long`
- updated JWT subject parsing to resolve authenticated users by `Long` id
- added Liquibase migration `08-convert-entity-ids-to-long.xml` to convert primary keys and foreign keys to `bigint identity`
- preserved existing foreign-key behavior and indexes after id conversion
- updated tests for generated database ids

Notes:

- Docker runner container names still use UUID values; this is not a persisted entity identifier.
- the id conversion changes public API id shapes from UUID strings to numeric values and requires frontend compatibility updates

Dependencies:

- feature/docker-sandbox-hardening

---

## 12. feature/solution-class-submissions

Status: [x] Completed

Goal:

Use LeetCode-style Java submissions where users edit a `class Solution` with a task-specific method signature.

Delivered:

- added task method metadata: `methodName`, `methodReturnType`, and `methodParameters`
- exposed method metadata in admin and public task DTOs
- updated Java runner wrapper to compile submitted `class Solution` source
- runner now invokes the configured task method with Java argument expressions from test case input
- normalized primitive, object, and array return values before comparing with expected output
- added Liquibase migration `09-add-task-method-signature.xml`
- migrated existing task code fragments into `class Solution { public String solve(String input) ... }`
- updated runner, controller, repository, and API tests

Notes:

- frontend should render starter code as a full non-public `class Solution`
- test case `input` now represents the Java argument expression used in the method call, for example `123, 456` for `totalWaviness(int num1, int num2)`
- expected output remains a string representation of the method result

Dependencies:

- feature/entity-long-ids

---

## 13. feature/frontend-response-formatting

Status: [x] Completed

Goal:

Normalize API response values for frontend display.

Delivered:

- formatted frontend-facing `createdAt` and `updatedAt` values as `HH:mm:ss yyyy-MM-dd`
- rounded submission `executionDurationMs` values to two significant digits
- added shared MapStruct response formatting helper
- added mapper and submission API tests for formatted values

Notes:

- timestamps are formatted from UTC instants to preserve the previous `Z`-based response time
- duration formatting keeps the existing numeric `executionDurationMs` contract

Dependencies:

- feature/solution-class-submissions

---

## 14. feature/admin-task-create-fields

Status: [x] Completed

Goal:

Keep admin task creation compatible with Solution-class task authoring fields.

Delivered:

- made explicit task method metadata optional in admin task create/update requests
- derived `methodName`, `methodReturnType`, and `methodParameters` from `class Solution` source when omitted
- validated that explicit metadata matches the parsed Solution method signature
- added controller and resolver tests for method metadata derivation

Notes:

- this keeps existing frontend admin forms compatible while preserving the backend method metadata required by submissions
- invalid Solution source without a resolvable method returns `VALIDATION_ERROR`

Dependencies:

- feature/frontend-response-formatting

---

## 15. feature/developers-by-salary-signature

Status: [x] Completed

Goal:

Update `developers-by-salary` to use a clearer Solution method contract.

Delivered:

- renamed task method from `solve` to `developerNamesBySalary`
- changed task method input from `String input` to `String[] developers`
- removed `throws Exception` from task starter and solution templates
- updated task test case input to a Java `String[]` expression
- allowed backend task signature parsing to ignore optional `throws ...` declarations

Notes:

- the Java runner wrapper already declares `main` with `throws Exception`, so task methods do not need to expose checked exceptions in starter code
- migration `10-update-developers-by-salary-signature.xml` updates existing local task data when the slug is present

Dependencies:

- feature/admin-task-create-fields

---

## Recommended Order

1. feature/auth [x]
2. feature/tasks-domain [x]
3. feature/admin-tasks-api [x]
4. feature/public-tasks-api [x]
5. feature/submissions-domain [x]
6. feature/java-code-runner [x]
7. feature/submission-api [x]
8. feature/progress [x]
9. feature/admin-submissions [x]
10. feature/docker-sandbox-hardening [x]
11. feature/entity-long-ids [x]
12. feature/solution-class-submissions [x]
13. feature/frontend-response-formatting [x]
14. feature/admin-task-create-fields [x]
15. feature/developers-by-salary-signature [x]
