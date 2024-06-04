package com.sample.ibanpaymentvalidator.controllers;

import com.sample.ibanpaymentvalidator.domain.Payment;
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
    public List<Payment> createPayment(@RequestBody Payment payment) {
        return paymentService.createPayment(payment);
    }
    @GetMapping("/payments")
    public List<Payment> getPayments(@RequestParam(required = false) String debtorIban) {
        if(debtorIban != null) {
            return paymentService.getPaymentsByDebtorIban(debtorIban);
        } else{
            return paymentService.getPayments();
        }
    }
    @PostMapping("/payment-files")
    public List<Payment> createPaymentFromCsv(@RequestParam("file") MultipartFile file) {
       return paymentService.createPaymentFromCsv(file);
    }

    @GetMapping("/test")
    public String testEndpoint() {
        return "Endpoint is working!";
    }
}
