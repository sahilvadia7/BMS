package com.bms.account.feign;

import com.bms.account.dtos.CustomerRegisterRequestDTO;
import com.bms.account.dtos.CustomerResponseDTO;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "customer-service",url = "http://localhost:8082/api/v1/customers")
public interface CustomerClient {

//    @GetMapping("/{id}")
//    CustomerResponseDTO getCustomerById(@PathVariable Long id);

//    @GetMapping("/{id}/exists")
//    Boolean customerExists(@PathVariable Long id) ;

    @PostMapping("/register")
    CustomerResponseDTO registerCustomer(@Valid @RequestBody CustomerRegisterRequestDTO requestDTO) ;
}
