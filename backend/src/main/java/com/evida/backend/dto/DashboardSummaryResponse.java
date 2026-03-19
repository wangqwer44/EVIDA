package com.evida.backend.dto;

import java.math.BigDecimal;
import java.util.Map;

public record DashboardSummaryResponse(long totalExpenses, long rejectedExpenses, long manualCheckExpenses,
                                       long clarificationNeededExpenses, long submittedExpenses, BigDecimal totalAmount,
                                       Map<String, BigDecimal> departmentSpend, Map<String, Long> statusCounts) {
}