package com.sample.ibanpaymentvalidator.utils;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigInteger;

public class IBANValidation {


    public static boolean isValidIban(String iban) {
        if (iban.length() >= 15 && iban.length() <= 34) {
            String temporaryIban = iban.substring(4);
            temporaryIban += iban.substring(0, 4);
            char[] ibanChars = temporaryIban.toCharArray();
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < ibanChars.length; i++) {
                if (Character.isLetter(ibanChars[i])) {
                    result.append(Character.getNumericValue(ibanChars[i]));
                } else if (Character.isDigit(ibanChars[i])) {
                    result.append(ibanChars[i]);
                }
            }
            BigInteger bigInteger = new BigInteger(result.toString()).mod(new BigInteger("97"));
            return bigInteger.intValue() == 1;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "IBAN is not valid");
        }
    }
}
