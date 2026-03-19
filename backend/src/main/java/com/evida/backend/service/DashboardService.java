package com.evida.backend.service;

import com.evida.backend.dto.DashboardSummaryResponse;
import com.evida.backend.entity.Expense;
import com.evida.backend.enums.ExpenseStatus;
import com.evida.backend.repository.ExpenseRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    private final ExpenseRepository expenseRepository;

    public DashboardService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public DashboardSummaryResponse getSummary() {
        List<Expense> expenses = expenseRepository.findAll();
        BigDecimal totalAmount = expenses.stream()
                .map(Expense::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, BigDecimal> departmentSpend = expenses.stream()
                .filter(expense -> expense.getAmount() != null)
                .collect(Collectors.groupingBy(Expense::getDepartment,
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)));
        Map<String, Long> statusCounts = expenses.stream()
                .collect(Collectors.groupingBy(expense -> expense.getStatus().name(), Collectors.counting()));

        return new DashboardSummaryResponse(
                expenses.size(),
                count(expenses, ExpenseStatus.REJECTED),
                count(expenses, ExpenseStatus.MANUAL_CHECK),
                count(expenses, ExpenseStatus.NEEDS_CLARIFICATION),
                count(expenses, ExpenseStatus.SUBMITTED),
                totalAmount,
                departmentSpend,
                statusCounts
        );
    }

    private long count(List<Expense> expenses, ExpenseStatus status) {
        return expenses.stream().filter(e -> e.getStatus() == status).count();
    }
}