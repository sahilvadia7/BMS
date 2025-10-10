package com.bms.customer.controller;

import com.bms.customer.dtos.kyc.KycRequestDTO;
import com.bms.customer.dtos.kyc.KycResponseDTO;
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
@Tag(name = "KYC APIs", description = "CRUD operations for KYC management")
public class KycController {

    private final KycService kycService;

    @Operation(summary = "Create new KYC record")
    @PostMapping
    public ResponseEntity<KycResponseDTO> createKyc(@Valid @RequestBody KycRequestDTO requestDTO) {
        return new ResponseEntity<>(kycService.createKyc(requestDTO), HttpStatus.CREATED);
    }

    @Operation(summary = "Get KYC by ID")
    @GetMapping("/{id}")
    public ResponseEntity<KycResponseDTO> getKycById(@PathVariable Long id) {
        return ResponseEntity.ok(kycService.getKycById(id));
    }

    @Operation(summary = "Get all KYC records")
    @GetMapping
    public ResponseEntity<List<KycResponseDTO>> getAllKyc() {
        return ResponseEntity.ok(kycService.getAllKyc());
    }

    @Operation(summary = "Update KYC by ID")
    @PutMapping("/{id}")
    public ResponseEntity<KycResponseDTO> updateKyc(@PathVariable Long id, @Valid @RequestBody KycRequestDTO requestDTO) {
        return ResponseEntity.ok(kycService.updateKyc(id, requestDTO));
    }

    @Operation(summary = "Delete KYC by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteKyc(@PathVariable Long id) {
        kycService.deleteKyc(id);
        return ResponseEntity.noContent().build();
    }
}
