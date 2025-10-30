package com.bms.customer.services;

import com.bms.customer.dtos.kyc.KycRequestDTO;
import com.bms.customer.dtos.kyc.KycResponseDTO;
import com.bms.customer.dtos.response.CustomerResponseDTO;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

public interface KycService {

//    KycResponseDTO createKycDocument(KycRequestDTO requestDTO);
    KycResponseDTO getKycById(Long id);
    List<KycResponseDTO> getAllKyc();
    KycResponseDTO updateKyc(Long id, KycRequestDTO requestDTO);
    void deleteKyc(Long id);
    CustomerResponseDTO linkKycToCustomer(Long customerId, Long kycId);
    CustomerResponseDTO approveKyc(Long kycId, String approvedBy);
    void rejectKyc(Long kycId, String reason);

    KycResponseDTO uploadKycForCustomer(Long customerId, KycRequestDTO requestDTO);

    KycResponseDTO getKycByCustomerId(Long customerId);

    boolean existsByCustomer_CustomerId(Long customerId);


    Long findKycIdByCustomerId(Long customerId);
}