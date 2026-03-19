package com.evida.backend.service;

import com.evida.backend.enums.ReceiptType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReceiptAnalysisResult(String merchantName, String category, BigDecimal amount,
                                    LocalDate expenseDate, LocalDateTime paymentTime,
                                    ReceiptType receiptType, String aiSummary) {
}