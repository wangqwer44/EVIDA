package com.evida.backend.service;

import com.evida.backend.dto.CreateExpenseRequest;
import com.evida.backend.dto.ExpenseResponse;
import com.evida.backend.dto.FinanceReviewRequest;
import com.evida.backend.dto.SubmitExpenseRequest;
import com.evida.backend.entity.Expense;
import com.evida.backend.enums.AuditAction;
import com.evida.backend.enums.AuditSeverity;
import com.evida.backend.enums.ExpenseStatus;
import com.evida.backend.repository.ExpenseRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final StorageService storageService;
    private final AiAnalysisClient aiAnalysisClient;
    private final MatchingEngine matchingEngine;
    private final PolicyEngine policyEngine;
    private final AuditLogService auditLogService;

    public ExpenseService(ExpenseRepository expenseRepository,
                          StorageService storageService,
                          AiAnalysisClient aiAnalysisClient,
                          MatchingEngine matchingEngine,
                          PolicyEngine policyEngine,
                          AuditLogService auditLogService) {
        this.expenseRepository = expenseRepository;
        this.storageService = storageService;
        this.aiAnalysisClient = aiAnalysisClient;
        this.matchingEngine = matchingEngine;
        this.policyEngine = policyEngine;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public ExpenseResponse createExpense(CreateExpenseRequest request, MultipartFile file) {
        String storageKey = storageService.store(file);

        Expense expense = new Expense();
        expense.setEmployeeId(request.getEmployeeId());
        expense.setEmployeeName(request.getEmployeeName());
        expense.setDepartment(request.getDepartment());
        expense.setOriginalFilename(file.getOriginalFilename() == null ? "receipt" : file.getOriginalFilename());
        expense.setStorageKey(storageKey);
        expense.setStatus(ExpenseStatus.ANALYZING);
        expense = expenseRepository.save(expense);
        auditLogService.log(expense.getId(), AuditAction.UPLOADED, AuditSeverity.INFO, "영수증 업로드 완료");

        ReceiptAnalysisResult analysis = aiAnalysisClient.analyze(storageKey, expense.getOriginalFilename(), request);
        expense.setMerchantName(analysis.merchantName());
        expense.setCategory(analysis.category());
        expense.setAmount(analysis.amount());
        expense.setExpenseDate(analysis.expenseDate());
        expense.setPaymentTime(analysis.paymentTime());
        expense.setReceiptType(analysis.receiptType());
        expense.setAiSummary(analysis.aiSummary());
        auditLogService.log(expense.getId(), AuditAction.ANALYZED, AuditSeverity.INFO, analysis.aiSummary());

        MatchingResult matchingResult = matchingEngine.match(expense);
        expense.setMatchingScore(matchingResult.score());
        expense.setMatchedTransactionReference(matchingResult.matchedReference());

        PolicyEvaluationResult evaluation = policyEngine.evaluate(expense);
        expense.setStatus(evaluation.getStatus());
        expense.setDeductible(evaluation.isDeductible());
        expense.setClarificationMessage(evaluation.getClarificationMessage());
        expense = expenseRepository.save(expense);

        if (!evaluation.isDeductible()) {
            auditLogService.log(expense.getId(), AuditAction.DEDUCTIBILITY_MARKED_FALSE, AuditSeverity.WARNING, "불공제 태그 추가");
        }
        if (evaluation.getStatus() == ExpenseStatus.REJECTED) {
            auditLogService.log(expense.getId(), AuditAction.RULE_REJECTED, AuditSeverity.CRITICAL, "세무 룰 자동 반려");
        } else if (evaluation.getStatus() == ExpenseStatus.MANUAL_CHECK) {
            auditLogService.log(expense.getId(), AuditAction.MANUAL_CHECK_REQUIRED, AuditSeverity.WARNING, "수동 검토 필요");
        } else if (evaluation.getStatus() == ExpenseStatus.NEEDS_CLARIFICATION) {
            auditLogService.log(expense.getId(), AuditAction.CLARIFICATION_REQUESTED, AuditSeverity.WARNING, "소명 요청 필요");
        } else {
            auditLogService.log(expense.getId(), AuditAction.READY_TO_SUBMIT, AuditSeverity.INFO, "제출 준비 완료");
        }

        return toResponse(expense);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> findExpenses(String department, ExpenseStatus status) {
        List<Expense> expenses;
        if (department != null && !department.isBlank()) {
            expenses = expenseRepository.findByDepartmentOrderByCreatedAtDesc(department);
        } else if (status != null) {
            expenses = expenseRepository.findByStatusOrderByCreatedAtDesc(status);
        } else {
            expenses = expenseRepository.findAll().stream()
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .toList();
        }
        return expenses.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getExpense(Long expenseId) {
        return toResponse(getExpenseEntity(expenseId));
    }

    @Transactional
    public ExpenseResponse submitExpense(Long expenseId, SubmitExpenseRequest request) {
        Expense expense = getExpenseEntity(expenseId);
        if (expense.getStatus() == ExpenseStatus.REJECTED || expense.getStatus() == ExpenseStatus.MANUAL_CHECK) {
            throw new IllegalArgumentException("현재 상태에서는 제출할 수 없습니다.");
        }
        if (expense.getStatus() == ExpenseStatus.NEEDS_CLARIFICATION
                && (request.getClarificationAnswer() == null || request.getClarificationAnswer().isBlank())) {
            throw new IllegalArgumentException("소명 답변이 필요합니다.");
        }
        expense.setClarificationAnswer(request.getClarificationAnswer());
        expense.setStatus(ExpenseStatus.SUBMITTED);
        expense.setSubmittedAt(LocalDateTime.now());
        expenseRepository.save(expense);
        auditLogService.log(expenseId, AuditAction.SUBMITTED, AuditSeverity.INFO, "결의 제출 완료");
        return toResponse(expense);
    }

    @Transactional
    public ExpenseResponse reviewExpense(Long expenseId, FinanceReviewRequest request) {
        Expense expense = getExpenseEntity(expenseId);
        if (request.isApproved()) {
            expense.setStatus(ExpenseStatus.FINANCE_APPROVED);
            auditLogService.log(expenseId, AuditAction.FINANCE_APPROVED, AuditSeverity.INFO, request.getReviewComment());
        } else {
            expense.setStatus(ExpenseStatus.FINANCE_REJECTED);
            auditLogService.log(expenseId, AuditAction.FINANCE_REJECTED, AuditSeverity.WARNING, request.getReviewComment());
        }
        expenseRepository.save(expense);
        return toResponse(expense);
    }

    private Expense getExpenseEntity(Long expenseId) {
        return expenseRepository.findById(expenseId)
                .orElseThrow(() -> new NotFoundException("지출 내역을 찾을 수 없습니다. id=" + expenseId));
    }

    private ExpenseResponse toResponse(Expense expense) {
        return ExpenseResponse.from(expense, auditLogService.findByExpenseId(expense.getId()));
    }
}