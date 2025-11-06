package com.bms.creditscore.feign;

import com.bms.creditscore.dto.extrenal.TransactionSummaryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "transaction-service", url = "${transaction.service.url:http://localhost:8084}")
public interface TransactionClient {
    @GetMapping("/api/v1/transactions/summary/{customerId}")
    TransactionSummaryDTO getTransactionSummary(@PathVariable("customerId") Long customerId);
}

