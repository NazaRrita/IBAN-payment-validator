package com.sample.ibanpaymentvalidator.repository;

import com.sample.ibanpaymentvalidator.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    @Query("SELECT p FROM Payment p WHERE p.id = :id AND p.debtorIban = :debtorIban " +
            "AND p.amount = :amount AND p.createdAt = :createdAt")
    List<Payment> getPayments(@Param("id") Integer id, @Param("debtorIban") String debtorIban,
                              @Param("amount") BigDecimal amount, @Param("createdAt") LocalDateTime createdAt);

    @Query("SELECT p FROM Payment p WHERE p.debtorIban = :debtorIban")
    List<Payment> findByDebtorIban(@Param("debtorIban") String debtorIban);
}
