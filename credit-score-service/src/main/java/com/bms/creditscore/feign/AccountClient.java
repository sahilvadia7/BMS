package com.bms.creditscore.feign;


import com.bms.creditscore.dto.extrenal.AccountSummaryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "account-service", url = "http://localhost:8084/api/v1/accounts")
public interface AccountClient {
    @GetMapping("/accounts/summary/{customerId}")
    AccountSummaryDTO getAccountSummary(@PathVariable("customerId") Long customerId);
}
