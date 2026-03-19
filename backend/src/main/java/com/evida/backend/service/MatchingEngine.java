package com.evida.backend.service;

import com.evida.backend.entity.CardTransaction;
import com.evida.backend.entity.Expense;
import com.evida.backend.repository.CardTransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MatchingEngine {
    private final CardTransactionRepository cardTransactionRepository;

    public MatchingEngine(CardTransactionRepository cardTransactionRepository) {
        this.cardTransactionRepository = cardTransactionRepository;
    }

    public MatchingResult match(Expense expense) {
        if (expense.getPaymentTime() == null || expense.getAmount() == null) {
            return new MatchingResult(0.0d, null);
        }
        LocalDateTime start = expense.getPaymentTime().minusDays(3);
        LocalDateTime end = expense.getPaymentTime().plusDays(3);
        List<CardTransaction> candidates = cardTransactionRepository.findByApprovedAtBetween(start, end);
        return candidates.stream()
                .map(candidate -> new CandidateScore(candidate, score(expense, candidate)))
                .max(Comparator.comparingDouble(CandidateScore::score))
                .map(best -> new MatchingResult(best.score(), best.transaction().getCardReference()))
                .orElse(new MatchingResult(0.0d, null));
    }

    private double score(Expense expense, CardTransaction candidate) {
        return amountSimilarity(expense.getAmount(), candidate.getAmount()) * 0.7d
                + dateSimilarity(expense.getPaymentTime(), candidate.getApprovedAt()) * 0.3d;
    }

    private double amountSimilarity(BigDecimal a, BigDecimal b) {
        BigDecimal diff = a.subtract(b).abs();
        if (diff.compareTo(BigDecimal.ZERO) == 0) return 1.0d;
        if (diff.compareTo(BigDecimal.valueOf(1000)) <= 0) return 0.9d;
        if (diff.compareTo(BigDecimal.valueOf(5000)) <= 0) return 0.6d;
        return 0.1d;
    }

    private double dateSimilarity(LocalDateTime a, LocalDateTime b) {
        long hours = Math.abs(ChronoUnit.HOURS.between(a, b));
        if (hours <= 2) return 1.0d;
        if (hours <= 24) return 0.8d;
        if (hours <= 72) return 0.5d;
        return 0.1d;
    }

    private record CandidateScore(CardTransaction transaction, double score) {
    }
}