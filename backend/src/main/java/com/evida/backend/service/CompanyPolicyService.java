package com.evida.backend.service;

import com.evida.backend.config.AppProperties;
import com.evida.backend.dto.CompanyPolicyRequest;
import com.evida.backend.entity.CompanyPolicy;
import com.evida.backend.repository.CompanyPolicyRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyPolicyService {
    private final CompanyPolicyRepository companyPolicyRepository;
    private final AppProperties appProperties;

    public CompanyPolicyService(CompanyPolicyRepository companyPolicyRepository, AppProperties appProperties) {
        this.companyPolicyRepository = companyPolicyRepository;
        this.appProperties = appProperties;
    }

    public List<CompanyPolicy> findAll() {
        return companyPolicyRepository.findAll();
    }

    public CompanyPolicy getPolicy(String department) {
        return companyPolicyRepository.findByDepartmentIgnoreCase(department).orElseGet(this::defaultPolicy);
    }

    @Transactional
    public CompanyPolicy upsertPolicy(CompanyPolicyRequest request) {
        CompanyPolicy policy = companyPolicyRepository.findByDepartmentIgnoreCase(request.getDepartment()).orElseGet(CompanyPolicy::new);
        policy.setDepartment(request.getDepartment());
        policy.setWeekendEntertainmentAllowed(Boolean.TRUE.equals(request.getWeekendEntertainmentAllowed()));
        policy.setLateNightEntertainmentAllowed(Boolean.TRUE.equals(request.getLateNightEntertainmentAllowed()));
        policy.setMonthlyBudgetLimit(request.getMonthlyBudgetLimit());
        policy.setAutoApprovalThreshold(request.getAutoApprovalThreshold() == 0.0d
                ? appProperties.getPolicy().getAutoApprovalThreshold()
                : request.getAutoApprovalThreshold());
        return companyPolicyRepository.save(policy);
    }

    private CompanyPolicy defaultPolicy() {
        CompanyPolicy policy = new CompanyPolicy();
        policy.setDepartment("DEFAULT");
        policy.setWeekendEntertainmentAllowed(false);
        policy.setLateNightEntertainmentAllowed(false);
        policy.setMonthlyBudgetLimit(BigDecimal.valueOf(5_000_000L));
        policy.setAutoApprovalThreshold(appProperties.getPolicy().getAutoApprovalThreshold());
        return policy;
    }
}