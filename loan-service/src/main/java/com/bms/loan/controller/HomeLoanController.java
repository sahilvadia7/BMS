package com.bms.loan.controller;

import com.bms.loan.dto.request.home.HomeVerificationRequestDto;
import com.bms.loan.dto.response.home.HomeVerificationResponse;
import com.bms.loan.service.HomeLoanService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/loans/home")
@RequiredArgsConstructor
public class HomeLoanController {

    private final HomeLoanService homeLoanService;

    // verify property
    @Operation(summary = "Verify home property and documents")
    @PostMapping("/verify")
    public ResponseEntity<HomeVerificationResponse> verifyProperty(@RequestBody HomeVerificationRequestDto request) {
        HomeVerificationResponse response = homeLoanService.verifyProperty(request);
        return ResponseEntity.ok(response);
    }

}
