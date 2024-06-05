package com.sample.ibanpaymentvalidator.controllers;

import com.sample.ibanpaymentvalidator.domain.Payment;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.sample.ibanpaymentvalidator.service.PaymentService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/payments")
    public List<Payment> createPayment(@RequestBody Payment payment, HttpServletRequest request) {
        String clientIp = paymentService.getClientIp(request);
        return paymentService.createPayment(payment, clientIp);
    }

    @GetMapping("/payments")
    public List<Payment> getPayments(@RequestParam(required = false) String debtorIban) {
        if (debtorIban != null) {
            return paymentService.getPaymentsByDebtorIban(debtorIban);
        } else {
            return paymentService.getPayments();
        }
    }

    @PostMapping("/payment-files")
    public List<Payment> createPaymentFromCsv(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        String clientIp = paymentService.getClientIp(request);
        return paymentService.createPaymentFromCsv(file, clientIp);
    }

    @GetMapping("/test")
    public String testEndpoint() {
        return "Endpoint is working!";
    }
}
