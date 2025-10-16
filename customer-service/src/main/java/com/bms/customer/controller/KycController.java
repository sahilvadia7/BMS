package com.bms.customer.controller;

import com.bms.customer.dtos.kyc.KycRequestDTO;
import com.bms.customer.dtos.kyc.KycResponseDTO;
import com.bms.customer.dtos.response.CustomerResponseDTO;
import com.bms.customer.services.KycService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/kyc")
@RequiredArgsConstructor
@Tag(name = "KYC APIs", description = "Endpoints for KYC document management and linking")
public class KycController {

    private final KycService kycService;

    @Operation(summary = "Submit new KYC document details")
    @PostMapping
    public ResponseEntity<KycResponseDTO> submitKycDocument(@Valid @RequestBody KycRequestDTO requestDTO) {
        return new ResponseEntity<>(kycService.createKycDocument(requestDTO), HttpStatus.CREATED);
    }

    @Operation(summary = "Admin: Get all KYC documents")
    @GetMapping
    public ResponseEntity<List<KycResponseDTO>> getAllKyc() {
        return ResponseEntity.ok(kycService.getAllKyc());
    }

    @Operation(summary = "Admin: Get KYC document by ID")
    @GetMapping("/{id}")
    public ResponseEntity<KycResponseDTO> getKycById(@PathVariable Long id) {
        return ResponseEntity.ok(kycService.getKycById(id));
    }

    @Operation(summary = "Admin: Update KYC document details (e.g., correct document number)")
    @PutMapping("/{id}")
    public ResponseEntity<KycResponseDTO> updateKyc(@PathVariable Long id, @Valid @RequestBody KycRequestDTO requestDTO) {
        return ResponseEntity.ok(kycService.updateKyc(id, requestDTO));
    }

    @Operation(summary = "Admin: Delete a KYC document")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteKyc(@PathVariable Long id) {
        kycService.deleteKyc(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Admin: Verify KYC and link to Customer, activating the account")
    @PatchMapping("/{customerId}/verify/{kycId}")
    public ResponseEntity<CustomerResponseDTO> verifyAndLinkKyc(
            @PathVariable Long customerId,
            @PathVariable Long kycId) {

        CustomerResponseDTO response = kycService.verifyAndLinkKyc(customerId, kycId);
        return ResponseEntity.ok(response);
    }
}