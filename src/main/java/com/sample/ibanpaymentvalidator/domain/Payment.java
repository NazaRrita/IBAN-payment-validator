package com.sample.ibanpaymentvalidator.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "payments")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Payment {
    @jakarta.persistence.Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "debtor_iban")
    private String debtorIban;
    private BigDecimal amount;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "country", nullable = true)
    private String country;

    public Payment(String debtorIban, BigDecimal amount, String country) {
        this.debtorIban = debtorIban;
        this.amount = amount;
        this.createdAt = LocalDateTime.now();
        this.country = country;
    }

    public Payment() {

    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDebtorIban() {
        return debtorIban;
    }

    public void setDebtorIban(String debtorIban) {
        this.debtorIban = debtorIban;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return Objects.equals(debtorIban, payment.debtorIban) && Objects.equals(amount, payment.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(debtorIban, amount);
    }


}
