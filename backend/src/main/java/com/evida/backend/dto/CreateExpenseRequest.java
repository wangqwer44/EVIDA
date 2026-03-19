package com.evida.backend.dto;

import com.evida.backend.enums.ReceiptType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CreateExpenseRequest {
    @NotBlank(message = "employeeId는 필수입니다.")
    private String employeeId;
    @NotBlank(message = "employeeName은 필수입니다.")
    private String employeeName;
    @NotBlank(message = "department는 필수입니다.")
    private String department;
    private String merchantHint;
    private String categoryHint;
    @DecimalMin(value = "0.0", inclusive = false, message = "amountHint는 0보다 커야 합니다.")
    private BigDecimal amountHint;
    private LocalDate expenseDateHint;
    private LocalDateTime paymentTimeHint;
    private ReceiptType receiptTypeHint;

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getMerchantHint() { return merchantHint; }
    public void setMerchantHint(String merchantHint) { this.merchantHint = merchantHint; }
    public String getCategoryHint() { return categoryHint; }
    public void setCategoryHint(String categoryHint) { this.categoryHint = categoryHint; }
    public BigDecimal getAmountHint() { return amountHint; }
    public void setAmountHint(BigDecimal amountHint) { this.amountHint = amountHint; }
    public LocalDate getExpenseDateHint() { return expenseDateHint; }
    public void setExpenseDateHint(LocalDate expenseDateHint) { this.expenseDateHint = expenseDateHint; }
    public LocalDateTime getPaymentTimeHint() { return paymentTimeHint; }
    public void setPaymentTimeHint(LocalDateTime paymentTimeHint) { this.paymentTimeHint = paymentTimeHint; }
    public ReceiptType getReceiptTypeHint() { return receiptTypeHint; }
    public void setReceiptTypeHint(ReceiptType receiptTypeHint) { this.receiptTypeHint = receiptTypeHint; }
}