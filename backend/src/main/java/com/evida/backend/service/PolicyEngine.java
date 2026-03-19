package com.evida.backend.service;

import com.evida.backend.config.AppProperties;
import com.evida.backend.entity.CompanyPolicy;
import com.evida.backend.entity.Expense;
import com.evida.backend.enums.ExpenseStatus;
import com.evida.backend.enums.ReceiptType;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class PolicyEngine {
    private final AppProperties appProperties;
    private final CompanyPolicyService companyPolicyService;

    public PolicyEngine(AppProperties appProperties, CompanyPolicyService companyPolicyService) {
        this.appProperties = appProperties;
        this.companyPolicyService = companyPolicyService;
    }

    public PolicyEvaluationResult evaluate(Expense expense) {
        CompanyPolicy policy = companyPolicyService.getPolicy(expense.getDepartment());
        PolicyEvaluationResult result = new PolicyEvaluationResult(ExpenseStatus.READY_TO_SUBMIT);

        if (expense.getReceiptType() == ReceiptType.MANUAL
                && expense.getAmount() != null
                && expense.getAmount().compareTo(BigDecimal.valueOf(30_000L)) > 0) {
            result.setStatus(ExpenseStatus.REJECTED);
            result.addAuditMessage("간이 영수증 3만원 초과 자동 반려");
            return result;
        }

        String merchant = expense.getMerchantName() == null ? "" : expense.getMerchantName().toLowerCase(Locale.ROOT);
        String category = expense.getCategory() == null ? "" : expense.getCategory().toLowerCase(Locale.ROOT);
        boolean nondeductible = appProperties.getPolicy().getNondeductibleKeywords().stream()
                .map(word -> word.toLowerCase(Locale.ROOT))
                .anyMatch(word -> merchant.contains(word) || category.contains(word));
        if (nondeductible) {
            result.setDeductible(false);
            result.addAuditMessage("불공제 키워드 감지");
        }

        if (expense.getPaymentTime() != null && (category.contains("접대") || category.contains("entertain"))) {
            boolean weekend = expense.getPaymentTime().getDayOfWeek() == DayOfWeek.SATURDAY
                    || expense.getPaymentTime().getDayOfWeek() == DayOfWeek.SUNDAY;
            boolean lateNight = expense.getPaymentTime().getHour() >= 22 || expense.getPaymentTime().getHour() < 6;
            if ((weekend && !policy.isWeekendEntertainmentAllowed()) || (lateNight && !policy.isLateNightEntertainmentAllowed())) {
                result.setStatus(ExpenseStatus.NEEDS_CLARIFICATION);
                result.setClarificationMessage("주말/심야 접대비 예외 적용 확인을 위해 소명이 필요합니다.");
                result.addAuditMessage("소명 요청 생성");
                return result;
            }
        }

        if (expense.getMatchingScore() == null || expense.getMatchingScore() < policy.getAutoApprovalThreshold()) {
            result.setStatus(ExpenseStatus.MANUAL_CHECK);
            result.addAuditMessage("매칭 점수 부족으로 수동 검토 전환");
            return result;
        }

        result.addAuditMessage("자동 승인 기준 충족");
        return result;
    }
}