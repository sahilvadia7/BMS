package com.bms.loan.feign;

import com.bms.loan.dto.request.transaction.TransactionRequest;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "transaction-service",url = "http://localhost:8085")
public interface TransactionClient {

    @PostMapping("/api/v1/transactions")
    Object createTransaction(@Valid @RequestBody TransactionRequest request);

}
