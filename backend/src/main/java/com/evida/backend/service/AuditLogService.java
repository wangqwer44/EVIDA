package com.evida.backend.service;

import com.evida.backend.dto.AuditLogResponse;
import com.evida.backend.entity.AuditLog;
import com.evida.backend.enums.AuditAction;
import com.evida.backend.enums.AuditSeverity;
import com.evida.backend.repository.AuditLogRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(Long expenseId, AuditAction action, AuditSeverity severity, String message) {
        auditLogRepository.save(new AuditLog(expenseId, action, severity, message));
    }

    public List<AuditLogResponse> findByExpenseId(Long expenseId) {
        return auditLogRepository.findByExpenseIdOrderByCreatedAtAsc(expenseId).stream()
                .map(AuditLogResponse::from)
                .toList();
    }
}