package com.evida.backend.dto;

import com.evida.backend.entity.CompanyPolicy;
import java.math.BigDecimal;

public record CompanyPolicyResponse(Long id, String department, boolean weekendEntertainmentAllowed,
                                    boolean lateNightEntertainmentAllowed, BigDecimal monthlyBudgetLimit,
                                    double autoApprovalThreshold) {
    public static CompanyPolicyResponse from(CompanyPolicy policy) {
        return new CompanyPolicyResponse(
                policy.getId(), policy.getDepartment(), policy.isWeekendEntertainmentAllowed(),
                policy.isLateNightEntertainmentAllowed(), policy.getMonthlyBudgetLimit(), policy.getAutoApprovalThreshold()
        );
    }
}