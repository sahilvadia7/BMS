package com.bms.account.feign;

import com.bms.account.dtos.AccountCreationNotificationRequest;
import com.bms.account.dtos.accountPin.OtpEmailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "notification-service",url = "http://localhost:8088/api/v1/notifications")
public interface NotificationClient {

    @PostMapping("/account-created")
    void sendAccountCreationEmail(AccountCreationNotificationRequest request);

    @PostMapping("/send-pin-otp")
    void sendOtpEmailPin(@RequestBody OtpEmailRequest request);
}
