package com.monadacademy.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.monadacademy.backend.entity.AuditLog;

/**
 * Provides persistence operations for audit log entries.
 *
 * @author Monad Academy Agent
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
