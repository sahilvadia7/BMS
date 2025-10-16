package com.bms.customer.services;

import com.bms.customer.dtos.kyc.KycRequestDTO;
import com.bms.customer.dtos.kyc.KycResponseDTO;
import com.bms.customer.dtos.response.CustomerResponseDTO;

import java.util.List;

public interface KycService {

    KycResponseDTO createKycDocument(KycRequestDTO requestDTO);
    KycResponseDTO getKycById(Long id);
    List<KycResponseDTO> getAllKyc();
    KycResponseDTO updateKyc(Long id, KycRequestDTO requestDTO);
    void deleteKyc(Long id);
    CustomerResponseDTO verifyAndLinkKyc(Long customerId, Long kycId);
}