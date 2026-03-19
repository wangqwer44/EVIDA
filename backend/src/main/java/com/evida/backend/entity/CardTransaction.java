package com.evida.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "card_transactions")
public class CardTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String cardReference;

    @Column(nullable = false)
    private String merchantName;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime approvedAt;

    public CardTransaction() {
    }

    public CardTransaction(String cardReference, String merchantName, BigDecimal amount, LocalDateTime approvedAt) {
        this.cardReference = cardReference;
        this.merchantName = merchantName;
        this.amount = amount;
        this.approvedAt = approvedAt;
    }

    public Long getId() { return id; }
    public String getCardReference() { return cardReference; }
    public String getMerchantName() { return merchantName; }
    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
}