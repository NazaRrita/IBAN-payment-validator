package com.sample.ibanpaymentvalidator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;


@Service
public class IpToCountryService {
    private static final Logger logger = LoggerFactory.getLogger(IpToCountryService.class);

    public String getClientCountry(String ip) {
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
            logger.error("Error while fetching country for ip: {}", ip, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while fetching country");
        }

    }
}
