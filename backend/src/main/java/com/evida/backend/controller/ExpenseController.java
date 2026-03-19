package com.evida.backend.controller;

import com.evida.backend.dto.CreateExpenseRequest;
import com.evida.backend.dto.ExpenseResponse;
import com.evida.backend.dto.FinanceReviewRequest;
import com.evida.backend.dto.SubmitExpenseRequest;
import com.evida.backend.enums.ExpenseStatus;
import com.evida.backend.service.ExpenseService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {
    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping(consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse createExpense(@Valid @ModelAttribute CreateExpenseRequest request,
                                         @RequestPart("file") MultipartFile file) {
        return expenseService.createExpense(request, file);
    }

    @GetMapping
    public List<ExpenseResponse> getExpenses(@RequestParam(required = false) String department,
                                             @RequestParam(required = false) ExpenseStatus status) {
        return expenseService.findExpenses(department, status);
    }

    @GetMapping("/{expenseId}")
    public ExpenseResponse getExpense(@PathVariable Long expenseId) {
        return expenseService.getExpense(expenseId);
    }

    @PostMapping("/{expenseId}/submit")
    public ExpenseResponse submitExpense(@PathVariable Long expenseId, @RequestBody(required = false) SubmitExpenseRequest request) {
        return expenseService.submitExpense(expenseId, request == null ? new SubmitExpenseRequest() : request);
    }

    @PostMapping("/{expenseId}/review")
    public ExpenseResponse reviewExpense(@PathVariable Long expenseId, @Valid @RequestBody FinanceReviewRequest request) {
        return expenseService.reviewExpense(expenseId, request);
    }
}