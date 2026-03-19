package com.evida.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.evida.backend.config.AppProperties;
import com.evida.backend.entity.CompanyPolicy;
import com.evida.backend.entity.Expense;
import com.evida.backend.enums.ExpenseStatus;
import com.evida.backend.enums.ReceiptType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PolicyEngineTest {

    private PolicyEngine policyEngine;

    @BeforeEach
    void setUp() {
        AppProperties properties = new AppProperties();
        properties.getPolicy().setAutoApprovalThreshold(0.95d);
        properties.getPolicy().setNondeductibleKeywords(List.of("ktx", "택시", "유흥"));
        CompanyPolicyService companyPolicyService = new CompanyPolicyService(null, properties) {
            @Override
            public CompanyPolicy getPolicy(String department) {
                return defaultPolicy();
            }
        };
        policyEngine = new PolicyEngine(properties, companyPolicyService);
    }

    @Test
    void rejectsManualReceiptOverThirtyThousand() {
        Expense expense = baseExpense();
        expense.setReceiptType(ReceiptType.MANUAL);
        expense.setAmount(BigDecimal.valueOf(31_000L));

        PolicyEvaluationResult result = policyEngine.evaluate(expense);

        assertThat(result.getStatus()).isEqualTo(ExpenseStatus.REJECTED);
    }

    @Test
    void requestsClarificationForWeekendEntertainmentWhenPolicyDisallowsIt() {
        Expense expense = baseExpense();
        expense.setCategory("접대비");
        expense.setPaymentTime(LocalDateTime.of(2026, 3, 15, 23, 30));
        expense.setMatchingScore(0.99d);

        PolicyEvaluationResult result = policyEngine.evaluate(expense);

        assertThat(result.getStatus()).isEqualTo(ExpenseStatus.NEEDS_CLARIFICATION);
    }

    private Expense baseExpense() {
        Expense expense = new Expense();
        expense.setDepartment("재무팀");
        expense.setAmount(BigDecimal.valueOf(20_000L));
        expense.setReceiptType(ReceiptType.CARD);
        expense.setPaymentTime(LocalDateTime.of(2026, 3, 16, 12, 0));
        expense.setMatchingScore(1.0d);
        expense.setMerchantName("일반가맹점");
        expense.setCategory("일반비용");
        return expense;
    }

    private CompanyPolicy defaultPolicy() {
        CompanyPolicy policy = new CompanyPolicy();
        policy.setDepartment("재무팀");
        policy.setWeekendEntertainmentAllowed(false);
        policy.setLateNightEntertainmentAllowed(false);
        policy.setMonthlyBudgetLimit(BigDecimal.valueOf(1_000_000L));
        policy.setAutoApprovalThreshold(0.95d);
        return policy;
    }
}