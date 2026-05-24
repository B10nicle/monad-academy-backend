# feature/auth

## Goal

Implement a complete authentication and authorization foundation for Monad Academy backend.

The result of this feature must provide:

- registration
- email verification
- resend verification email
- login
- JWT authentication
- current user endpoint
- audit logging
- consistent error responses
- automated tests

This feature is the foundation for future modules such as tasks, submissions, progress tracking, and admin functionality.

## Technical Context

Backend stack:

- Java 25
- Spring Boot 4
- Spring Security
- Spring Data JPA
- PostgreSQL
- Liquibase
- JWT authentication
- JUnit 5
- Mockito
- AssertJ

Code style requirements:

- use `var` wherever possible
- no comments in production or test code
- test naming style:

```java
testRegisterWhenEmailAlreadyExistsShouldThrowException
```

- use Mockito annotations:

```java
@Mock
@ExtendWith(MockitoExtension.class)
```

- annotations ordered from shortest to longest:

```java
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
```

## Functional Requirements

### 1. User model

Implement:

- `User`
- `UserRole`
- `UserStatus`

#### UserRole

- `USER`
- `ADMIN`

#### UserStatus

- `PENDING_EMAIL_VERIFICATION`
- `ACTIVE`
- `BLOCKED`
- `DELETED`

#### User entity fields

- id
- email
- username
- passwordHash
- role
- status
- emailVerifiedAt
- lastLoginAt
- createdAt
- updatedAt

Requirements:

- email unique
- username unique
- timestamps persisted
- UUID identifiers

### 2. Liquibase migrations

Create migrations for:

- users
- email_verification_tokens
- audit_logs

#### users

Requirements:

- unique(email)
- unique(username)
- indexes:
  - role
  - status

#### email_verification_tokens

Fields:

- id
- user_id
- token_hash
- expires_at
- confirmed_at
- created_at

Requirements:

- never store raw token
- persist only token_hash

#### audit_logs

Fields:

- id
- user_id
- event_type
- metadata
- created_at

### 3. Audit log

Implement:

- `AuditLog`
- `AuditEventType`
- `AuditLogRepository`
- `AuditLogService`

Supported events:

- USER_REGISTERED
- USER_EMAIL_VERIFIED
- USER_VERIFICATION_EMAIL_RESENT
- USER_LOGIN_SUCCEEDED
- USER_LOGIN_FAILED

### 4. Email verification token service

Implement:

- `EmailVerificationTokenService`

Responsibilities:

- generate raw token
- hash token
- persist token hash
- validate token
- check expiration
- mark token confirmed
- activate user

Requirements:

- configurable TTL
- reject expired, unknown, or confirmed token

### 5. Email sender abstraction

Implement:

- `EmailSender`
- `LoggingEmailSender`

Configuration:

```yaml
app:
  frontend-base-url: http://localhost:3000
  email-verification-token-ttl: 24h
```

Verification link:

```text
{frontend-base-url}/verify-email?token={token}
```

### 6. Registration API

Endpoint:

`POST /api/auth/register`

Request:

```json
{
  "email": "user@example.com",
  "username": "b10nicle",
  "password": "password"
}
```

Behavior:

- validate request
- check duplicate email
- check duplicate username
- hash password with BCrypt
- create USER
- status = PENDING_EMAIL_VERIFICATION
- generate verification token
- send email
- write audit log

Validation:

- email required
- valid email format
- username required
- password required
- min password length = 8

### 7. Verify email API

Endpoint:

`POST /api/auth/verify-email`

Request:

```json
{
  "token": "..."
}
```

Behavior:

- hash token
- find token_hash
- validate token
- activate user
- set emailVerifiedAt
- set confirmedAt
- write audit log

Result:

- `user.status = ACTIVE`

### 8. Resend verification API

Endpoint:

`POST /api/auth/resend-verification`

Request:

```json
{
  "email": "user@example.com"
}
```

Behavior:

- if user exists and not active:
  - create new token
  - send email
  - write audit log

Always return neutral response:

```json
{
  "message": "If the email exists, verification instructions have been sent"
}
```

### 9. Login API

Endpoint:

`POST /api/auth/login`

Request:

```json
{
  "login": "user@example.com",
  "password": "password"
}
```

Requirements:

- login via email or username

Behavior:

- find user
- validate password
- check status
- issue JWT
- update lastLoginAt
- write audit log

Status handling:

- PENDING_EMAIL_VERIFICATION → EMAIL_NOT_VERIFIED
- BLOCKED → USER_BLOCKED
- DELETED → INVALID_CREDENTIALS

### 10. JWT security

Implement:

- `JwtTokenService`
- `JwtAuthenticationFilter`
- `SecurityConfig`
- `CurrentUser`
- `CurrentUserProvider`

Public endpoints:

- POST /api/auth/register
- POST /api/auth/login
- POST /api/auth/verify-email
- POST /api/auth/resend-verification
- GET /actuator/health
- GET /actuator/info

All other endpoints require authentication.

JWT requirements:

- include userId
- include role
- configurable secret
- configurable expiration

### 11. Current user endpoint

Endpoint:

`GET /api/users/me`

Response:

```json
{
  "id": "...",
  "email": "user@example.com",
  "username": "b10nicle",
  "role": "USER",
  "status": "ACTIVE"
}
```

### 12. Error handling

Format:

```json
{
  "code": "EMAIL_ALREADY_EXISTS",
  "message": "Email is already registered"
}
```

Supported codes:

- VALIDATION_ERROR
- EMAIL_ALREADY_EXISTS
- USERNAME_ALREADY_EXISTS
- INVALID_CREDENTIALS
- EMAIL_NOT_VERIFIED
- USER_BLOCKED
- INVALID_VERIFICATION_TOKEN
- EXPIRED_VERIFICATION_TOKEN

### 13. Automated tests

Minimum:

- register creates pending user
- register rejects duplicate email
- register rejects duplicate username
- verify email activates user
- verify email rejects expired token
- resend verification does not reveal user existence
- login succeeds for active user
- login fails for unverified user
- login fails for wrong password
- `/api/users/me` works with JWT

## Implementation Order

1. Liquibase migrations
2. Entities / enums / repositories
3. DTO + validation
4. Audit log
5. Email verification token service
6. Email sender abstraction
7. Registration API
8. Verify email API
9. Resend verification API
10. Login API
11. JWT security
12. `/api/users/me`
13. Global exception handling
14. Tests

## Deliverables

- compilable project
- all tests green
- Liquibase migrations added
- feature implemented end-to-end
- no TODOs
- no commented code
- minimal clean architecture aligned with repository style
