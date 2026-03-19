package com.evida.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class CompanyPolicyRequest {
    @NotBlank(message = "department는 필수입니다.")
    private String department;
    @NotNull(message = "weekendEntertainmentAllowed는 필수입니다.")
    private Boolean weekendEntertainmentAllowed;
    @NotNull(message = "lateNightEntertainmentAllowed는 필수입니다.")
    private Boolean lateNightEntertainmentAllowed;
    @NotNull(message = "monthlyBudgetLimit는 필수입니다.")
    @DecimalMin(value = "0.0", message = "monthlyBudgetLimit는 0 이상이어야 합니다.")
    private BigDecimal monthlyBudgetLimit;
    @DecimalMin(value = "0.0", message = "autoApprovalThreshold는 0 이상이어야 합니다.")
    private double autoApprovalThreshold;

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public Boolean getWeekendEntertainmentAllowed() { return weekendEntertainmentAllowed; }
    public void setWeekendEntertainmentAllowed(Boolean weekendEntertainmentAllowed) { this.weekendEntertainmentAllowed = weekendEntertainmentAllowed; }
    public Boolean getLateNightEntertainmentAllowed() { return lateNightEntertainmentAllowed; }
    public void setLateNightEntertainmentAllowed(Boolean lateNightEntertainmentAllowed) { this.lateNightEntertainmentAllowed = lateNightEntertainmentAllowed; }
    public BigDecimal getMonthlyBudgetLimit() { return monthlyBudgetLimit; }
    public void setMonthlyBudgetLimit(BigDecimal monthlyBudgetLimit) { this.monthlyBudgetLimit = monthlyBudgetLimit; }
    public double getAutoApprovalThreshold() { return autoApprovalThreshold; }
    public void setAutoApprovalThreshold(double autoApprovalThreshold) { this.autoApprovalThreshold = autoApprovalThreshold; }
}