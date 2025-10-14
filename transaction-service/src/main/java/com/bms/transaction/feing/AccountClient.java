package com.bms.transaction.feing;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "account-service", path = "/api/v1/accounts")
public interface AccountClient {

    @GetMapping("/{accountId}")
    boolean getAccountById(@PathVariable("accountId") Long accountId);

    @GetMapping("/{accountId}/balance")
    BigDecimal getAccountBalance(@PathVariable("accountId") Long accountId);

    @PostMapping("/{accountId}/balance")
    void updateBalance(
            @PathVariable("accountId") Long accountId,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam("transactionType") String transactionType
    );
}