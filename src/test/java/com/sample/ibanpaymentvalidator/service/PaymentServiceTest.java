package com.sample.ibanpaymentvalidator.service;

import com.sample.ibanpaymentvalidator.domain.Payment;
import com.sample.ibanpaymentvalidator.repository.PaymentRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    PaymentRepository paymentRepository;
    @Mock
    IpToCountryService ipToCountryService;

    @InjectMocks
    PaymentService paymentservice;
    private Map<String, String> ipCountryMap;

    @BeforeEach
    void setUp() {
        ipCountryMap = new HashMap<>();
        ipCountryMap.put("102.38.247.25", "Lithuania");
        ipCountryMap.put("85.206.12.124", "Latvia");
        ipCountryMap.put("82.131.33.249", "Estonia");
        ipCountryMap.put("192.168.1.1", "Unknown");
    }

    @Test
    void shouldCreatePayment() {
        Payment testPayment = new Payment("LT601010012345678901", BigDecimal.valueOf(200.00), "Lithuania");
        String ip = "102.38.247.25";
        ipToCountryPlaceholder(ip);

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
    void shouldReturnPaymentsList() {
        Payment payment1 = new Payment("LV97HABA0012345678910", BigDecimal.valueOf(1000.00), "Latvia");
        Payment payment2 = new Payment("LT601010012345678901", BigDecimal.valueOf(200.00), "Lithuania");
        Payment payment3 = new Payment("EE471000001020145685", BigDecimal.valueOf(100.00), "Estonia");
        String ip1 = "102.38.247.25";
        String ip2 = "85.206.12.124";
        String ip3 = "82.131.33.249";

        ipToCountryPlaceholder(ip1);
        ipToCountryPlaceholder(ip2);
        ipToCountryPlaceholder(ip3);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.findAll()).thenReturn(List.of(payment1, payment2, payment3));

        paymentservice.createPayment(payment1, ip1);
        paymentservice.createPayment(payment2, ip2);
        paymentservice.createPayment(payment3, ip3);

        List<Payment> payments = paymentservice.getPayments();

        Assertions.assertEquals(3, payments.size());
        Assertions.assertEquals("LV97HABA0012345678910", payments.get(0).getDebtorIban());
        Assertions.assertEquals(BigDecimal.valueOf(200.00), payments.get(1).getAmount());
        Assertions.assertEquals("Estonia", payments.get(2).getCountry());
    }

    @Test
    void shouldFindPaymentByDebtorIban() {
        Payment payment1 = new Payment("LV97HABA0012345678910", BigDecimal.valueOf(1000.00), "Latvia");
        Payment payment2 = new Payment("LT601010012345678901", BigDecimal.valueOf(200.00), "Lithuania");
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

    @Test
    void shouldCreatePaymentFromCsv() {
        String csv = "amount,debtorIban\n15.0,LT356437978869712537";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", csv.getBytes());
        String ip = "85.206.12.124";
        Payment payment = new Payment("LT356437978869712537", BigDecimal.valueOf(15.00), "Lithuania");

        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        List<Payment> createdPayments = paymentservice.createPaymentFromCsv(file, ip);

        Assertions.assertEquals(1, createdPayments.size());

        Payment createdPayment = createdPayments.getFirst();
        Assertions.assertEquals(payment.getDebtorIban(), createdPayment.getDebtorIban());
        Assertions.assertEquals(payment.getAmount(), createdPayment.getAmount());
        Assertions.assertEquals("Lithuania", createdPayment.getCountry());
    }

    @Test
    void shouldValidateClientCountry() {
        String country1 = null;
        String country2 = "Lithuania";
        String country3 = "";
        String country4 = "Belgium";

        Assertions.assertFalse(paymentservice.isValidCountry(country1));
        Assertions.assertTrue(paymentservice.isValidCountry(country2));
        Assertions.assertFalse(paymentservice.isValidCountry(country3));
        Assertions.assertFalse(paymentservice.isValidCountry(country4));
    }

    @Test
    void shouldGetClientIp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "102.38.247.25");
        String ip = paymentservice.getClientIp(request);
        Assertions.assertEquals("102.38.247.25", ip);
    }

    private void ipToCountryPlaceholder(String ip) {
        when(ipToCountryService.getClientCountry(ip)).thenAnswer(invocation -> {
            String ipArg = invocation.getArgument(0);
            return ipCountryMap.getOrDefault(ipArg, "Unknown");
        });
    }

}