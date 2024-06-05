package com.sample.ibanpaymentvalidator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sample.ibanpaymentvalidator.domain.Payment;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
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
    private PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public List<Payment> createPayment(Payment payment, String clientIp) {
        List<Payment> createdPayments = new ArrayList<>();
        String iban = ibanValidation(payment.getDebtorIban());
        BigDecimal validatedAmount = isValidAmount(payment.getAmount());
        String country = getClientCountry(clientIp);
        if (!isValidCountry(country)) {
            payment.setCountry(country);
        }
        Payment newPayment = paymentRepository.save(new Payment(iban, validatedAmount, country));
        createdPayments.add(newPayment);
        return createdPayments;
    }

    private String ibanValidation(String debtorIban) {
        String regex = "^(LV|LT|EE)$";
        if (IBANValidation.isValidIban(debtorIban) && (debtorIban.substring(0, 2).matches(regex))) {
            return debtorIban;
        } else {
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

    public List<Payment> createPaymentFromCsv(MultipartFile file, String clientIp) {

        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }
        List<Payment> createdPayments = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()));
            String line;
            boolean isFirstLine = true;
            while ((line = br.readLine()) != null) {
                line = line.replace("\ufeff", "").replaceAll("\"", "");
                if (isFirstLine) {
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
                    createdPayments.addAll(createPayment(payment, clientIp));
                }
            }
        } catch (IOException e) {
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

    private String getClientCountry(String ip) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject("http://ip-api.com/json/" + ip, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(response);
            String country = node.path("country").asText();
            if (country == null || country.isEmpty()) {
                return null;
            }
            return country;
        } catch (Exception e) {
            return null;
        }

    }

    private boolean isValidCountry(String country) {
        if (country == null || country.isEmpty()) {
            return false;
        }
        return (country.equalsIgnoreCase("Latvia")
                || country.equalsIgnoreCase("Lithuania")
                || country.equalsIgnoreCase("Estonia"));
    }

}
