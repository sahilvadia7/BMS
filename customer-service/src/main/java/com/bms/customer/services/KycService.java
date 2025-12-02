package com.bms.customer.services;

import com.bms.customer.dtos.kyc.KycRequestDTO;
import com.bms.customer.dtos.kyc.KycResponseDTO;
import com.bms.customer.dtos.response.CustomerDetailsResponseDTO;

import java.util.List;

public interface KycService {

//    KycResponseDTO createKycDocument(KycRequestDTO requestDTO);
    KycResponseDTO getKycById(Long id);
    List<KycResponseDTO> getAllKyc();
    KycResponseDTO updateKyc(Long id, KycRequestDTO requestDTO);
    void deleteKyc(Long id);
    CustomerDetailsResponseDTO linkKycToCustomer(Long customerId, Long kycId);
    CustomerDetailsResponseDTO approveKyc(Long kycId, String approvedBy);
    void rejectKyc(Long kycId, String reason);

    KycResponseDTO uploadKycForCustomer(Long customerId, KycRequestDTO requestDTO);

    KycResponseDTO getKycByCustomerId(Long customerId);

    boolean existsByCustomer_CustomerId(Long customerId);


    Long findKycIdByCustomerId(Long customerId);
}