package com.sample.ibanpaymentvalidator.service;

import com.sample.ibanpaymentvalidator.domain.Payment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.sample.ibanpaymentvalidator.repository.PaymentRepository;
import com.sample.ibanpaymentvalidator.utils.IBANValidation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentService {
    public PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public List <Payment> createPayment(Payment payment) {
        List <Payment> createdPayments = new ArrayList<>();
        String iban = ibanValidation(payment.getDebtorIban());
        BigDecimal validatedAmount = isValidAmount(payment.getAmount());
        Payment newPayment = paymentRepository.save(new Payment(iban, validatedAmount));
        createdPayments.add(newPayment);
        return createdPayments;
    }
    private String ibanValidation(String debtorIban) {
        if (IBANValidation.isValidIban(debtorIban)) {
            return debtorIban;
        }else{
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid IBAN");
        }
    }

    private BigDecimal isValidAmount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            return amount;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid amount");
        }
    }
    public List<Payment> createPaymentFromCsv(MultipartFile file)  {

        if(file.isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }
        List<Payment> createdPayments = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()));
            String line;
            boolean isFirstLine = true;
            while ((line = br.readLine()) != null) {
                line = line.replace("\ufeff", "").replaceAll("\"", "");
                if(isFirstLine){
                    isFirstLine = false;
                    continue;
                }

                String[] data = line.split(",");
                if (data.length == 2) {
                    String debtorIban = data[1].trim();
                    String amount = data[0].trim();
                    Payment payment = new Payment();
                    payment.setDebtorIban(debtorIban);
                    payment.setAmount(new BigDecimal(amount));
                    createdPayments.addAll(createPayment(payment));
                }
            }
        }catch (IOException e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while reading file");
        }
        return createdPayments;
    }
    public List<Payment> getPayments() {
        return paymentRepository.findAll();
    }

    public List<Payment> getPaymentsByDebtorIban(String debtorIban) {
        return paymentRepository.findByDebtorIban(debtorIban);
    }
}
