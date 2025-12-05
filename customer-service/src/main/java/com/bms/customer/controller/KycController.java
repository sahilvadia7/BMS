package com.bms.customer.controller;

import com.bms.customer.dtos.kyc.KycRequestDTO;
import com.bms.customer.dtos.kyc.KycResponseDTO;
import com.bms.customer.dtos.kyc.KycUploadRequestDto;
import com.bms.customer.dtos.response.CustomerResponseDTO;
import com.bms.customer.services.KycService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/kyc")
@Tag(name = "KYC APIs", description = "Endpoints for KYC document management and linking")
public class KycController {

    private final KycService kycService;

    public KycController(KycService kycService) {
        this.kycService = kycService;
    }

    // @Operation(summary = "Submit new KYC document details")
    // @PostMapping
    // public ResponseEntity<KycResponseDTO> submitKycDocument(@Valid @RequestBody
    // KycRequestDTO requestDTO) {
    // return new ResponseEntity<>(kycService.createKycDocument(requestDTO),
    // HttpStatus.CREATED);
    // }

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

    @Operation(summary = "Check if KYC exists for given customer ID")
    @GetMapping("/exists/{customerId}")
    public ResponseEntity<Long> checkKycExists(@PathVariable Long customerId) {
        Long kycId = kycService.findKycIdByCustomerId(customerId);
        return ResponseEntity.ok(kycId);
    }

    @Operation(summary = "Admin: Update KYC document details (e.g., correct document number)")
    @PutMapping("/{id}")
    public ResponseEntity<KycResponseDTO> updateKyc(@PathVariable Long id,
            @Valid @RequestBody KycRequestDTO requestDTO) {
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

        CustomerResponseDTO response = kycService.linkKycToCustomer(customerId, kycId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Customer: Upload KYC document")
    @PostMapping("/upload")
    public ResponseEntity<KycResponseDTO> uploadKyc(@Valid @RequestBody KycUploadRequestDto dto) {
        return ResponseEntity.ok(kycService.uploadKycForCustomer(dto.getCustomerId(), dto.getKyc()));
    }

    @Operation(summary = "Branch Manager: Approve KYC and activate customer")
    // @PreAuthorize("hasRole('BRANCH_MANAGER')")
    @PatchMapping("/{kycId}/approve")
    public ResponseEntity<CustomerResponseDTO> approveKyc(
            @PathVariable Long kycId,
            @RequestHeader(value = "X-Approved-By", required = false) String approvedBy) {

        String approver = approvedBy != null ? approvedBy : "system@test.com";
        return ResponseEntity.ok(kycService.approveKyc(kycId, approver));
    }

    @Operation(summary = "Branch Manager: Reject KYC")
    // @PreAuthorize("hasRole('BRANCH_MANAGER')")
    @PatchMapping("/{kycId}/reject")
    public ResponseEntity<Void> rejectKyc(
            @PathVariable Long kycId,
            @RequestParam String reason) {
        kycService.rejectKyc(kycId, reason);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all KYC documents with status for given Customer ID")
    @GetMapping("/status/{customerId}")
    public ResponseEntity<List<KycResponseDTO>> findAllKycStatusByCustomerId(@PathVariable Long customerId) {
        List<KycResponseDTO> responseList = kycService.getAllKycStatusByCustomerId(customerId);
        return ResponseEntity.ok(responseList);
    }
}