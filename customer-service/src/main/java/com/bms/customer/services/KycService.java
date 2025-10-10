package com.bms.customer.services;

import com.bms.customer.dtos.kyc.KycRequestDTO;
import com.bms.customer.dtos.kyc.KycResponseDTO;

import java.util.List;

public interface KycService {

    KycResponseDTO createKyc(KycRequestDTO requestDTO);

    KycResponseDTO getKycById(Long id);

    List<KycResponseDTO> getAllKyc();

    KycResponseDTO updateKyc(Long id, KycRequestDTO requestDTO);

    void deleteKyc(Long id);
}
