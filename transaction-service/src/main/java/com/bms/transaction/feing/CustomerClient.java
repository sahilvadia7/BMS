package com.bms.transaction.feing;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "customer-service", path = "/api/v1/customers")
public interface CustomerClient {

	@GetMapping("/info/{cifNumber}")
	ResponseEntity<Map<String, Object>> getLimitedInfoByCif(@PathVariable String cifNumber);

}
