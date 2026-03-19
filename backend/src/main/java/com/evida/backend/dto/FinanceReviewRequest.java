package com.evida.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class FinanceReviewRequest {
    private boolean approved;
    @NotBlank(message = "reviewComment는 필수입니다.")
    private String reviewComment;
    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }
    public String getReviewComment() { return reviewComment; }
    public void setReviewComment(String reviewComment) { this.reviewComment = reviewComment; }
}