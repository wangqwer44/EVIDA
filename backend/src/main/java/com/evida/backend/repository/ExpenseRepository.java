package com.evida.backend.repository;

import com.evida.backend.entity.Expense;
import com.evida.backend.enums.ExpenseStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByDepartmentOrderByCreatedAtDesc(String department);

    List<Expense> findByStatusOrderByCreatedAtDesc(ExpenseStatus status);
}