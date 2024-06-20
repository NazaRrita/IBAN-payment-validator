package com.sample.ibanpaymentvalidator.utils;

import com.opencsv.exceptions.CsvException;
import com.sample.ibanpaymentvalidator.domain.Payment;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PaymentParser {

    public static List<Payment> parseCsvFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }
        List<Payment> payments = new ArrayList<>();

        try {
            Reader reader = new InputStreamReader(file.getInputStream());
            CSVParser csvParser = new CSVParserBuilder()
                    .withSeparator(',')
                    .withIgnoreQuotations(true)
                    .build();

            CSVReader csvReader = new CSVReaderBuilder(reader)
                    .withSkipLines(1)
                    .withCSVParser(csvParser)
                    .build();

            String[] line;
            while ((line = csvReader.readNext()) != null) {
                if (line.length == 2) {
                    String debtorIban = line[1].trim().replaceAll("\"", "");
                    String amount = line[0].trim().replaceAll("\"", "");
                    Payment payment = new Payment();
                    payment.setDebtorIban(debtorIban);
                    payment.setAmount(new BigDecimal(amount));
                    payments.add(payment);
                }
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while reading file");
        } catch (CsvException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error while parsing CSV file");
        }

        return payments;
    }
}
