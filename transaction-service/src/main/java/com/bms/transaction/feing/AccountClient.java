package com.bms.transaction.feing;

import com.bms.transaction.dto.response.AccountResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "ACCOUNT-SERVICE", url = "http://localhost:8084/api/v1/accounts")
public interface AccountClient {

    @GetMapping("/{accountNumber}/balance")
    BigDecimal getBalance(@PathVariable String accountNumber);

    @PostMapping("/{accountNumber}/balance")
    BigDecimal updateBalance(
            @PathVariable String accountNumber,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam("transactionType") String transactionType
    );

    @PostMapping("/verify-pin")
    Boolean verifyAccountPin(
            @RequestParam String accountNumber,
            @RequestParam int accountPin
    );

    @GetMapping("/exists/{accountNumber}")
    Boolean accountExists(@PathVariable String accountNumber);

    @GetMapping("/number/{accountNumber}")
    AccountResponseDTO getAccountByNumber(@PathVariable String accountNumber);
}
