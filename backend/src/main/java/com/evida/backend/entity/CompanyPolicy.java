package com.evida.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "company_policies")
public class CompanyPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String department;

    @Column(nullable = false)
    private boolean weekendEntertainmentAllowed;

    @Column(nullable = false)
    private boolean lateNightEntertainmentAllowed;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyBudgetLimit = BigDecimal.ZERO;

    @Column(nullable = false)
    private double autoApprovalThreshold = 0.95d;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public boolean isWeekendEntertainmentAllowed() { return weekendEntertainmentAllowed; }
    public void setWeekendEntertainmentAllowed(boolean weekendEntertainmentAllowed) { this.weekendEntertainmentAllowed = weekendEntertainmentAllowed; }
    public boolean isLateNightEntertainmentAllowed() { return lateNightEntertainmentAllowed; }
    public void setLateNightEntertainmentAllowed(boolean lateNightEntertainmentAllowed) { this.lateNightEntertainmentAllowed = lateNightEntertainmentAllowed; }
    public BigDecimal getMonthlyBudgetLimit() { return monthlyBudgetLimit; }
    public void setMonthlyBudgetLimit(BigDecimal monthlyBudgetLimit) { this.monthlyBudgetLimit = monthlyBudgetLimit; }
    public double getAutoApprovalThreshold() { return autoApprovalThreshold; }
    public void setAutoApprovalThreshold(double autoApprovalThreshold) { this.autoApprovalThreshold = autoApprovalThreshold; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}