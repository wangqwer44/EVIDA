package com.evida.backend.service;

import com.evida.backend.dto.CompanyPolicyRequest;
import com.evida.backend.entity.CardTransaction;
import com.evida.backend.repository.CardTransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private final CompanyPolicyService companyPolicyService;
    private final CardTransactionRepository cardTransactionRepository;

    public DataInitializer(CompanyPolicyService companyPolicyService, CardTransactionRepository cardTransactionRepository) {
        this.companyPolicyService = companyPolicyService;
        this.cardTransactionRepository = cardTransactionRepository;
    }

    @Override
    public void run(String... args) {
        if (companyPolicyService.findAll().isEmpty()) {
            companyPolicyService.upsertPolicy(policy("영업팀", true, true, BigDecimal.valueOf(10_000_000L), 0.95));
            companyPolicyService.upsertPolicy(policy("재무팀", false, false, BigDecimal.valueOf(6_000_000L), 0.97));
        }
        if (cardTransactionRepository.count() == 0) {
            cardTransactionRepository.save(new CardTransaction("CARD-001", "일반가맹점", BigDecimal.valueOf(18_000L), LocalDateTime.now().minusHours(1)));
            cardTransactionRepository.save(new CardTransaction("CARD-002", "KTX", BigDecimal.valueOf(59_800L), LocalDateTime.now().minusDays(1)));
        }
    }

    private CompanyPolicyRequest policy(String dept, boolean weekend, boolean late, BigDecimal limit, double threshold) {
        CompanyPolicyRequest request = new CompanyPolicyRequest();
        request.setDepartment(dept);
        request.setWeekendEntertainmentAllowed(weekend);
        request.setLateNightEntertainmentAllowed(late);
        request.setMonthlyBudgetLimit(limit);
        request.setAutoApprovalThreshold(threshold);
        return request;
    }
}