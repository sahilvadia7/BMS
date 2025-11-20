package com.bms.transaction.feing;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "account-service", path = "/api/v1/accounts")
public interface AccountClient {

    @GetMapping("/{accountNumber}/balance")
     ResponseEntity<BigDecimal> getBalance(@PathVariable String accountNumber);

    @PostMapping("/{accountNumber}/balance")
    ResponseEntity<?> updateBalance(
            @PathVariable String accountNumber,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam("transactionType") String transactionType);

    @PostMapping("/verify-pin")
    ResponseEntity<Boolean> verifyAccountPin(
            @RequestParam String accountNumber,
            @RequestParam int accountPin);


    @GetMapping("/exists/{accountNumber}")
    ResponseEntity<Boolean> accountExists(@PathVariable String accountNumber);
}