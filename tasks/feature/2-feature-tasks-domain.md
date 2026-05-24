# feature/tasks-domain

## Goal

Implement domain model for coding tasks.

## Entities

- Task
- TaskDifficulty
- TaskTopic
- TaskStatus
- TaskTestCase

## Requirements

- Liquibase migrations
- Repositories
- DTOs and validation
- UUID identifiers
- Audit timestamps

## Task fields

- id
- title
- slug
- description
- difficulty
- topic
- status
- initialCode
- solutionTemplate
- createdAt
- updatedAt

## TaskTestCase fields

- id
- taskId
- input
- expectedOutput
- hidden
- orderIndex
- createdAt

## Acceptance Criteria

- Task entity persisted
- Task test cases persisted
- Liquibase migrations added
- Repositories covered by tests

## Deliverables

- compilable project
- tests green
- feature implemented end-to-end
- no TODOs
- no commented code
