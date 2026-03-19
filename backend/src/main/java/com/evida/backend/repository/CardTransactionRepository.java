package com.evida.backend.repository;

import com.evida.backend.entity.CardTransaction;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardTransactionRepository extends JpaRepository<CardTransaction, Long> {

    List<CardTransaction> findByApprovedAtBetween(LocalDateTime start, LocalDateTime end);
}