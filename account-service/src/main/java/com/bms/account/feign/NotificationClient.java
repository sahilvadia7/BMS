package com.bms.account.feign;

import com.bms.account.dtos.AccountCloseRequestNotification;
import com.bms.account.dtos.AccountClosureDecisionNotification;
import com.bms.account.dtos.AccountCreationNotificationRequest;
import com.bms.account.dtos.AccountStatusChangeNotificationRequest;
import com.bms.account.dtos.accountPin.OtpEmailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "notification-service",url = "http://localhost:8089/api/v1/notifications")
public interface NotificationClient {

    @PostMapping("/account-created")
    void sendAccountCreationEmail(AccountCreationNotificationRequest request);

    @PostMapping("/send-pin-otp")
    void sendOtpEmailPin(@RequestBody OtpEmailRequest request);

    @PostMapping("/account-status-changed")
    void sendAccountStatusChangedEmail(
            @RequestBody AccountStatusChangeNotificationRequest request);

    @PostMapping("/account-close-requested")
    void sendAccountCloseRequestEmail(
            @RequestBody AccountCloseRequestNotification request
    );
    @PostMapping("/account-closure-decision")
    public void sendAccountClosureDecisionEmail(
            @RequestBody AccountClosureDecisionNotification request);
}
