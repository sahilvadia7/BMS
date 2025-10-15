package com.bms.loan.feign;

import com.bms.loan.dto.response.AccountResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "account-service", url = "http://localhost:8084/api/v1/accounts")
public interface AccountClient {

    @GetMapping("/number/{accountNumber}")
    AccountResponseDTO getAccountByNumber(@PathVariable String accountNumber);

}
