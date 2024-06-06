package com.sample.ibanpaymentvalidator.service;

import com.sample.ibanpaymentvalidator.domain.Payment;
import com.sample.ibanpaymentvalidator.repository.PaymentRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    PaymentRepository paymentRepository;

    @InjectMocks
    PaymentService paymentservice;

    @Test
    void shouldCreatePayment() {
    Payment testPayment = new Payment("LT601010012345678901", BigDecimal.valueOf(200.00), "Lithuania");
    String ip = "102.38.247.25";

        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        List<Payment> createdPayments = paymentservice.createPayment(testPayment, ip);

        Assertions.assertEquals(1, createdPayments.size());

        Payment createdPayment = createdPayments.getFirst();
        Assertions.assertEquals(testPayment.getDebtorIban(), createdPayment.getDebtorIban());
        Assertions.assertEquals(testPayment.getAmount(), createdPayment.getAmount());
        Assertions.assertEquals("Lithuania", createdPayment.getCountry());
    }

    @Test
    void shouldFailToCreatePaymentIfAmountIsNegative() {
        Payment testPayment = new Payment("LV97HABA0012345678910", BigDecimal.valueOf(1000.00), "Latvia");
        String ip = "102.38.247.25";
        testPayment.setAmount(BigDecimal.valueOf(-10.00));

        ResponseStatusException exception = Assertions.assertThrows(ResponseStatusException.class,
                () -> paymentservice.createPayment(testPayment, ip));
        Assertions.assertEquals(400, exception.getStatusCode().value());

    }
    @Test
    void shouldReturnPaymentsList(){
        Payment payment1 = new Payment("LV97HABA0012345678910", BigDecimal.valueOf(1000.00), "Latvia");
        Payment payment2 = new Payment ("LT601010012345678901", BigDecimal.valueOf(200.00), "Lithuania");
        Payment payment3 = new Payment ("EE471000001020145685", BigDecimal.valueOf(100.00), "Estonia");

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.findAll()).thenReturn(List.of(payment1, payment2, payment3));

        paymentservice.createPayment(payment1, "102.38.247.25");
        paymentservice.createPayment(payment2, "85.206.12.124");
        paymentservice.createPayment(payment3, "82.131.33.249");

        List<Payment> payments = paymentservice.getPayments();

        Assertions.assertEquals(3, payments.size());
        Assertions.assertEquals("LV97HABA0012345678910", payments.get(0).getDebtorIban());
        Assertions.assertEquals(BigDecimal.valueOf(200.00), payments.get(1).getAmount());
        Assertions.assertEquals("Estonia", payments.get(2).getCountry());
    }
    @Test
    void shouldFindPaymentByDebtorIban(){
        Payment payment1 = new Payment("LV97HABA0012345678910", BigDecimal.valueOf(1000.00), "Latvia");
        Payment payment2 = new Payment ("LT601010012345678901", BigDecimal.valueOf(200.00), "Lithuania");
        Payment payment3 = new Payment("LV97HABA0012345678910", BigDecimal.valueOf(105.00), "Latvia");

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.findByDebtorIban("LV97HABA0012345678910")).thenReturn(List.of(payment1, payment3));

        paymentservice.createPayment(payment1, "102.38.247.25");
        paymentservice.createPayment(payment2, "85.206.12.124");
        paymentservice.createPayment(payment3, "102.38.247.25");

        List<Payment> foundPayments = paymentservice.getPaymentsByDebtorIban("LV97HABA0012345678910");
        Assertions.assertEquals(2, foundPayments.size());

        foundPayments.forEach(payment ->
                Assertions.assertEquals("LV97HABA0012345678910", payment.getDebtorIban()));
    }
}