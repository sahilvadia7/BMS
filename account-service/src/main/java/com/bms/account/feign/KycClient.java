//package com.bms.account.feign;
//
//import com.bms.account.dtos.KycRequestDTO;
//import com.bms.account.dtos.KycResponseDTO;
//import jakarta.validation.Valid;
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestParam;
//
//@FeignClient(name = "customer-service",url = "http://localhost:8082/api/v1/customers")
//public interface KycClient {
//
//    @PostMapping("/upload")
//    KycResponseDTO uploadKyc(
//            @RequestParam Long customerId,
//            @Valid @RequestBody KycRequestDTO dto);
//}
