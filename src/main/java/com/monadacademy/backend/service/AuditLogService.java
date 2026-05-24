package com.monadacademy.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.monadacademy.backend.entity.AuditEventType;
import com.monadacademy.backend.entity.AuditLog;
import com.monadacademy.backend.entity.User;
import com.monadacademy.backend.repository.AuditLogRepository;

@Service
public class AuditLogService {

	private final AuditLogRepository auditLogRepository;

	public AuditLogService(AuditLogRepository auditLogRepository) {
		this.auditLogRepository = auditLogRepository;
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public void log(User user, AuditEventType eventType, String metadata) {
		auditLogRepository.save(new AuditLog(user, eventType, metadata));
	}
}
