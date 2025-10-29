package com.bms.loan.feign;

import com.bms.loan.dto.request.CustomerRegisterRequest;
import com.bms.loan.dto.response.CustomerDetailsResponseDTO;
import com.bms.loan.dto.response.CustomerResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "customer-service",url = "http://localhost:8082/api/v1/customers")
public interface CustomerClient {

    @PostMapping("/register")
    CustomerResponseDTO registerCustomer(@Valid @RequestBody CustomerRegisterRequest requestDTO) ;

    @GetMapping("/{id}")
    CustomerResponseDTO getById(@PathVariable Long id);


    @GetMapping("/cif/{cifNumber}")
    CustomerDetailsResponseDTO getByCif(@PathVariable String cifNumber);

}
