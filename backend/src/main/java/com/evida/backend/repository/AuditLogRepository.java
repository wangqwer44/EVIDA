package com.evida.backend.repository;

import com.evida.backend.entity.AuditLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByExpenseIdOrderByCreatedAtAsc(Long expenseId);
}