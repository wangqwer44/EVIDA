package com.evida.backend.service;

import com.evida.backend.enums.ExpenseStatus;
import java.util.ArrayList;
import java.util.List;

public class PolicyEvaluationResult {
    private ExpenseStatus status;
    private boolean deductible = true;
    private String clarificationMessage;
    private final List<String> auditMessages = new ArrayList<>();

    public PolicyEvaluationResult(ExpenseStatus status) { this.status = status; }
    public ExpenseStatus getStatus() { return status; }
    public void setStatus(ExpenseStatus status) { this.status = status; }
    public boolean isDeductible() { return deductible; }
    public void setDeductible(boolean deductible) { this.deductible = deductible; }
    public String getClarificationMessage() { return clarificationMessage; }
    public void setClarificationMessage(String clarificationMessage) { this.clarificationMessage = clarificationMessage; }
    public List<String> getAuditMessages() { return auditMessages; }
    public void addAuditMessage(String message) { this.auditMessages.add(message); }
}