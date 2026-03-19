package com.evida.backend.entity;

import com.evida.backend.enums.ExpenseStatus;
import com.evida.backend.enums.ReceiptType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String employeeId;

    @Column(nullable = false)
    private String employeeName;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private String originalFilename;

    @Column(nullable = false, unique = true)
    private String storageKey;

    private String merchantName;

    private String category;

    @Column(precision = 15, scale = 2)
    private BigDecimal amount;

    private LocalDate expenseDate;

    private LocalDateTime paymentTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReceiptType receiptType = ReceiptType.UNKNOWN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseStatus status = ExpenseStatus.UPLOADED;

    @Column(nullable = false)
    private boolean deductible = true;

    private Double matchingScore;

    @Column(length = 1000)
    private String aiSummary;

    @Column(length = 1000)
    private String clarificationMessage;

    @Column(length = 1000)
    private String clarificationAnswer;

    private String matchedTransactionReference;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime submittedAt;

    public Long getId() { return id; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
    public String getStorageKey() { return storageKey; }
    public void setStorageKey(String storageKey) { this.storageKey = storageKey; }
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }
    public LocalDateTime getPaymentTime() { return paymentTime; }
    public void setPaymentTime(LocalDateTime paymentTime) { this.paymentTime = paymentTime; }
    public ReceiptType getReceiptType() { return receiptType; }
    public void setReceiptType(ReceiptType receiptType) { this.receiptType = receiptType; }
    public ExpenseStatus getStatus() { return status; }
    public void setStatus(ExpenseStatus status) { this.status = status; }
    public boolean isDeductible() { return deductible; }
    public void setDeductible(boolean deductible) { this.deductible = deductible; }
    public Double getMatchingScore() { return matchingScore; }
    public void setMatchingScore(Double matchingScore) { this.matchingScore = matchingScore; }
    public String getAiSummary() { return aiSummary; }
    public void setAiSummary(String aiSummary) { this.aiSummary = aiSummary; }
    public String getClarificationMessage() { return clarificationMessage; }
    public void setClarificationMessage(String clarificationMessage) { this.clarificationMessage = clarificationMessage; }
    public String getClarificationAnswer() { return clarificationAnswer; }
    public void setClarificationAnswer(String clarificationAnswer) { this.clarificationAnswer = clarificationAnswer; }
    public String getMatchedTransactionReference() { return matchedTransactionReference; }
    public void setMatchedTransactionReference(String matchedTransactionReference) { this.matchedTransactionReference = matchedTransactionReference; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

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