package com.evida.backend.controller;

import com.evida.backend.dto.CompanyPolicyRequest;
import com.evida.backend.dto.CompanyPolicyResponse;
import com.evida.backend.service.CompanyPolicyService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/policies")
public class PolicyController {
    private final CompanyPolicyService companyPolicyService;

    public PolicyController(CompanyPolicyService companyPolicyService) {
        this.companyPolicyService = companyPolicyService;
    }

    @GetMapping
    public List<CompanyPolicyResponse> getPolicies() {
        return companyPolicyService.findAll().stream().map(CompanyPolicyResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyPolicyResponse upsertPolicy(@Valid @RequestBody CompanyPolicyRequest request) {
        return CompanyPolicyResponse.from(companyPolicyService.upsertPolicy(request));
    }
}