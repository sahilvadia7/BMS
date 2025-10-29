package com.bms.account.feign;

import com.bms.account.dtos.CustomerResponseDTO;
import com.bms.account.dtos.KycResponseDTO;
import com.bms.account.dtos.KycUploadRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "customer-service", url = "http://localhost:8082/api/v1")
public interface CustomerClient {

    // ✅ Calls CustomerController -> /api/v1/customers/cif/{cifNumber}
    @GetMapping("/customers/cif/{cifNumber}")
    CustomerResponseDTO getByCif(@PathVariable String cifNumber);

    @PostMapping(value = "/kyc/upload", consumes = "application/json")
    KycResponseDTO uploadKyc(@RequestBody KycUploadRequest request);
}
