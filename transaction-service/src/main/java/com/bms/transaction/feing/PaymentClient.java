package com.bms.transaction.feing;

import com.bms.transaction.dto.request.PaymentRequest;
import com.bms.transaction.dto.response.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
		name = "payment-gateway",
		path = "/api/v1/payments"
)
public interface PaymentClient {

	@PostMapping("/initiate")
	PaymentResponse initiatePayment(@RequestBody PaymentRequest request);
}

