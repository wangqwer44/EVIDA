package com.evida.backend.service;

import com.evida.backend.config.AppProperties;
import com.evida.backend.dto.CreateExpenseRequest;
import com.evida.backend.enums.ReceiptType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class AiAnalysisClient {
    private final WebClient webClient;
    private final AppProperties appProperties;

    public AiAnalysisClient(WebClient webClient, AppProperties appProperties) {
        this.webClient = webClient;
        this.appProperties = appProperties;
    }

    public ReceiptAnalysisResult analyze(String storageKey, String originalFilename, CreateExpenseRequest request) {
        if (appProperties.getAi().isUseLiveService()) {
            try {
                LiveAiResponse response = webClient.post()
                        .uri(appProperties.getAi().getBaseUrl() + "/analyze")
                        .bodyValue(Map.of("storageKey", storageKey, "originalFilename", originalFilename))
                        .retrieve()
                        .bodyToMono(LiveAiResponse.class)
                        .block();
                if (response != null) {
                    return new ReceiptAnalysisResult(
                            response.merchantName,
                            response.category,
                            response.amount,
                            response.expenseDate,
                            response.paymentTime,
                            response.receiptType == null ? ReceiptType.UNKNOWN : response.receiptType,
                            response.aiSummary
                    );
                }
            } catch (Exception ignored) {
            }
        }
        return fallback(originalFilename, request);
    }

    private ReceiptAnalysisResult fallback(String originalFilename, CreateExpenseRequest request) {
        String normalized = originalFilename == null ? "" : originalFilename.toLowerCase(Locale.ROOT);
        String merchant = request.getMerchantHint() != null ? request.getMerchantHint() : (normalized.contains("ktx") ? "KTX" : "일반가맹점");
        String category = request.getCategoryHint() != null ? request.getCategoryHint() : (normalized.contains("taxi") ? "교통비" : "일반비용");
        BigDecimal amount = request.getAmountHint() != null ? request.getAmountHint() : BigDecimal.valueOf(18000);
        LocalDate expenseDate = request.getExpenseDateHint() != null ? request.getExpenseDateHint() : LocalDate.now();
        LocalDateTime paymentTime = request.getPaymentTimeHint() != null ? request.getPaymentTimeHint() : LocalDateTime.now();
        ReceiptType receiptType = request.getReceiptTypeHint() != null ? request.getReceiptTypeHint() : ReceiptType.CARD;
        return new ReceiptAnalysisResult(merchant, category, amount, expenseDate, paymentTime, receiptType,
                "Fallback OCR 결과: " + merchant + ", " + amount + "원");
    }

    private record LiveAiResponse(String merchantName, String category, BigDecimal amount,
                                  LocalDate expenseDate, LocalDateTime paymentTime,
                                  ReceiptType receiptType, String aiSummary) {
    }
}