package com.evida.backend.entity;

import com.evida.backend.enums.AuditAction;
import com.evida.backend.enums.AuditSeverity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long expenseId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditSeverity severity;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public AuditLog() {
    }

    public AuditLog(Long expenseId, AuditAction action, AuditSeverity severity, String message) {
        this.expenseId = expenseId;
        this.action = action;
        this.severity = severity;
        this.message = message;
    }

    public Long getId() { return id; }
    public Long getExpenseId() { return expenseId; }
    public AuditAction getAction() { return action; }
    public AuditSeverity getSeverity() { return severity; }
    public String getMessage() { return message; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}