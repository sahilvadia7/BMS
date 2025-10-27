package com.bms.customer.feign;

import com.bms.customer.dtos.request.EmailRequestDTO;
import com.bms.customer.dtos.resetpass.OtpEmailDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "notification-service",
        url = "http://localhost:8088",
        path = "/api/v1/notifications"
)
public interface NotificationClient {

    @PostMapping("/send-registration-email")
    ResponseEntity<String> sendRegistrationEmail(@RequestBody EmailRequestDTO request);

    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtpEmail(@RequestBody OtpEmailDTO request);
}