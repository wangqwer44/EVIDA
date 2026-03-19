package com.evida.backend.dto;

import com.evida.backend.entity.Expense;
import com.evida.backend.enums.ExpenseStatus;
import com.evida.backend.enums.ReceiptType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ExpenseResponse(
        Long id,
        String employeeId,
        String employeeName,
        String department,
        String merchantName,
        String category,
        BigDecimal amount,
        LocalDate expenseDate,
        LocalDateTime paymentTime,
        ReceiptType receiptType,
        ExpenseStatus status,
        boolean deductible,
        Double matchingScore,
        String clarificationMessage,
        String clarificationAnswer,
        String matchedTransactionReference,
        List<AuditLogResponse> auditLogs
) {
    public static ExpenseResponse from(Expense expense, List<AuditLogResponse> logs) {
        return new ExpenseResponse(
                expense.getId(), expense.getEmployeeId(), expense.getEmployeeName(), expense.getDepartment(),
                expense.getMerchantName(), expense.getCategory(), expense.getAmount(), expense.getExpenseDate(),
                expense.getPaymentTime(), expense.getReceiptType(), expense.getStatus(), expense.isDeductible(),
                expense.getMatchingScore(), expense.getClarificationMessage(), expense.getClarificationAnswer(),
                expense.getMatchedTransactionReference(), logs
        );
    }
}