package com.bms.loan.config;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class AadhaarVerificationService {

    private static final String API_URL = "https://api.apyhub.com/validate/aadhaar";
    private static final String API_KEY = "APY00YHU9D1bSnKuUXeuUm0sKaIEfemdIAc09zGlWYY4Yo6xdG34QUobPaEQZZPMKdfuMiKz";

    public boolean verifyAadhaar(String aadhaarNumber) {
        try {

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("apy-token", API_KEY);

            // body
            Map<String, String> body = new HashMap<>();
            body.put("aadhaar", aadhaarNumber);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            // request
            ResponseEntity<Map> response = restTemplate.exchange(API_URL, HttpMethod.POST, request, Map.class);

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null || !responseBody.containsKey("data")) {
                return false;
            }
            Object data = responseBody.get("data");
            if (data instanceof Boolean) {
                return (Boolean) data; // true = valid, false = invalid
            }

            // fallback (in case structure changes)
            return false;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean verifyPan(String docNumber) {
        return true;
    }
}
