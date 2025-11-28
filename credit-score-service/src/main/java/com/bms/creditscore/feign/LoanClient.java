package com.bms.creditscore.feign;

import com.bms.creditscore.dto.extrenal.CustomerTimelyPaidEmiResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "loan-service", url = "${loan.service.url:http://localhost:8086}")
public interface LoanClient {

    @GetMapping("/api/v1/loans/{cifNumber}/customer")
    CustomerTimelyPaidEmiResponseDTO customerTimelyPaidEmiDetails(@PathVariable("cifNumber") String cifNumber);
}
