package com.sample.ibanpaymentvalidator.utils;

import org.iban4j.IbanFormatException;
import org.iban4j.IbanUtil;
import org.iban4j.UnsupportedCountryException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class IBANValidation {
    private static final String countryRegex = "^(LV|LT|EE)$";

    public static void isValidIban(String iban) {
        try {
            IbanUtil.validate(iban);
            String countryCode = IbanUtil.getCountryCode(iban);
            if (!countryCode.matches(countryRegex)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid country code");
            }
        } catch (IbanFormatException | UnsupportedCountryException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid IBAN");
        }
    }
}
