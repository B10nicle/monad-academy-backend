package com.monadacademy.backend.service.audit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.monadacademy.backend.entity.AuditEventType;
import com.monadacademy.backend.entity.AuditLog;
import com.monadacademy.backend.entity.User;
import com.monadacademy.backend.repository.AuditLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Writes audit log events inside active transactions.
 *
 * @author Monad Academy Agent
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

	private final AuditLogRepository auditLogRepository;

	@Transactional(propagation = Propagation.MANDATORY)
	public void log(User user, AuditEventType eventType, String metadata) {
		auditLogRepository.save(new AuditLog(user, eventType, metadata));
		log.debug("Persisted audit event type={} userId={} metadata={}", eventType, userId(user), metadata);
	}

	private String userId(User user) {
		if (user == null) {
			return null;
		}
		return user.getId().toString();
	}
}
