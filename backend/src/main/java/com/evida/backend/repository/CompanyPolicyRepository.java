package com.evida.backend.repository;

import com.evida.backend.entity.CompanyPolicy;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyPolicyRepository extends JpaRepository<CompanyPolicy, Long> {

    Optional<CompanyPolicy> findByDepartmentIgnoreCase(String department);
}