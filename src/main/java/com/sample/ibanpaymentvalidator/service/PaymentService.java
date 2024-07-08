package com.sample.ibanpaymentvalidator.service;

import com.sample.ibanpaymentvalidator.domain.Payment;
import com.sample.ibanpaymentvalidator.utils.PaymentParser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.sample.ibanpaymentvalidator.repository.PaymentRepository;
import com.sample.ibanpaymentvalidator.utils.IBANValidation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentService {
    private PaymentRepository paymentRepository;
    private IpToCountryService ipToCountryService;

    public PaymentService(PaymentRepository paymentRepository, IpToCountryService ipToCountryService) {
        this.paymentRepository = paymentRepository;
        this.ipToCountryService = ipToCountryService;
    }

    public List<Payment> createPayment(Payment payment, String clientIp) {
        List<Payment> createdPayments = new ArrayList<>();
        String iban = payment.getDebtorIban();
        IBANValidation.isValidIban(iban);
        BigDecimal validatedAmount = isValidAmount(payment.getAmount());
        String country = ipToCountryService.getClientCountry(clientIp);
        if (!isValidCountry(country)) {
            payment.setCountry(country);
        }
        Payment newPayment = paymentRepository.save(new Payment(iban, validatedAmount, country));
        createdPayments.add(newPayment);
        return createdPayments;
    }

    private BigDecimal isValidAmount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            return amount;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid amount");
        }
    }

    public List<Payment> createPaymentFromCsv(MultipartFile file, String clientIp) {
        List<Payment> paymentsFromCsv = PaymentParser.parseCsvFile(file);
        List<Payment> createdPayments = new ArrayList<>();

        for (Payment payment : paymentsFromCsv) {
            createdPayments.addAll(createPayment(payment, clientIp));
        }
        return createdPayments;
    }

    public List<Payment> getPayments() {
        return paymentRepository.findAll();
    }

    public List<Payment> getPaymentsByDebtorIban(String debtorIban) {
        return paymentRepository.findByDebtorIban(debtorIban);
    }

    public String getClientIp(HttpServletRequest request) {
        String ip = "";
        if (request != null) {
            ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty()) {
                ip = request.getRemoteAddr();
            }
        }
        return ip;
    }

    protected boolean isValidCountry(String country) {
        if (country == null || country.isEmpty()) {
            return false;
        }
        return (country.equalsIgnoreCase("Latvia")
                || country.equalsIgnoreCase("Lithuania")
                || country.equalsIgnoreCase("Estonia"));
    }

}
