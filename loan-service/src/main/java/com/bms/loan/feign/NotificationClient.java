package com.bms.loan.feign;


import com.bms.loan.dto.email.ApplyLoanEmailDTO;
import com.bms.loan.dto.email.SanctionEmailDTO;
import com.bms.loan.dto.email.DisbursementEmailDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service",url = "http://localhost:8088/api/v1/notifications")
public interface NotificationClient {

    @PostMapping("/send-sanction")
    ResponseEntity<String> sendSanctionEmail(@RequestBody SanctionEmailDTO request)  ;

    @PostMapping("/send-disbursement")
    ResponseEntity<String> sendDisbursementEmail(@RequestBody DisbursementEmailDTO request);

    @PostMapping("/send-applyLoan")
    ResponseEntity<String> sendApplyLoanEmail(@RequestBody ApplyLoanEmailDTO request);
}
