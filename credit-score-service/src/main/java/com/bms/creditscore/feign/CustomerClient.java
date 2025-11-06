package com.bms.creditscore.feign;

import com.bms.creditscore.dto.extrenal.CustomerFeignDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customer-service", url = "http://localhost:8082/api/v1/customers")
public interface CustomerClient {

    @GetMapping("/{id}")
    public ResponseEntity<CustomerFeignDTO> getById(@PathVariable Long id);
//    CustomerFeignDTO getCustomerById(@PathVariable Long id);

    @GetMapping("/greet")
    public String greet();


}

