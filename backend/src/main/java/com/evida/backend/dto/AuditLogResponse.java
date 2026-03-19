package com.evida.backend.dto;

import com.evida.backend.entity.AuditLog;
import com.evida.backend.enums.AuditAction;
import com.evida.backend.enums.AuditSeverity;
import java.time.LocalDateTime;

public record AuditLogResponse(Long id, AuditAction action, AuditSeverity severity, String message, LocalDateTime createdAt) {
    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(log.getId(), log.getAction(), log.getSeverity(), log.getMessage(), log.getCreatedAt());
    }
}