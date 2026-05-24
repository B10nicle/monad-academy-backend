# feature/submissions-domain

## Goal

Submission persistence and lifecycle.

## Entities

- Submission
- SubmissionStatus

## SubmissionStatus

- PENDING
- RUNNING
- ACCEPTED
- WRONG_ANSWER
- COMPILATION_ERROR
- RUNTIME_ERROR
- TIME_LIMIT_EXCEEDED
- INTERNAL_ERROR

## Requirements

- Store sourceCode
- Store execution metadata
- Store execution duration
- Submission history

## Deliverables

- compilable project
- tests green
- feature implemented end-to-end
- no TODOs
- no commented code
