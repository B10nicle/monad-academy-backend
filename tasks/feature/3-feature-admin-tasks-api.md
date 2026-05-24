# feature/admin-tasks-api

## Goal

Admin CRUD and lifecycle management for tasks.

## Endpoints

- POST /api/admin/tasks
- PUT /api/admin/tasks/{id}
- POST /api/admin/tasks/{id}/publish
- POST /api/admin/tasks/{id}/archive
- POST /api/admin/tasks/{id}/test-cases

## Requirements

- ADMIN access only
- Validation of request payloads
- Audit logging
- Publish/archive lifecycle

## Acceptance Criteria

- Task creation works
- Task update works
- Task publication works
- Task archive works

## Deliverables

- compilable project
- tests green
- feature implemented end-to-end
- no TODOs
- no commented code
