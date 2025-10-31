package com.bms.customer.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ACCOUNT-SERVICE", url = "http://localhost:8084/api/v1/accounts")
public interface AccountClient {

    @PutMapping("/{cifNumber}/activate")
    String activateAccountByCif(@PathVariable("cifNumber") String cifNumber);
}
