package com.bms.account.feign;

import com.bms.account.dtos.CustomerResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customer-service",url = "http://localhost:8082/api/v1/customers")
public interface CustomerClient {

//    @GetMapping("/{id}")
//    CustomerResponseDTO getCustomerById(@PathVariable Long id);

    @GetMapping("/{id}/exists")
    Boolean customerExists(@PathVariable Long id) ;
}
