package com.bms.customer.dtos.response;

import com.bms.customer.enums.Gender;
import com.bms.customer.enums.Roles;
import com.bms.customer.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set; // Import Set

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDetailsResponseDTO {

    private Long customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNo;
    private Roles role;
    private UserStatus status;
    private Gender gender;
    private String cifNumber;
    private String address;
    private LocalDate dob;

    // --- ARCHITECTURAL CHANGE ---
    // REMOVED: private Long kycId;
    // ADDED: A collection of linked KYC documents (use a simplified DTO for the response)
    private Set<CustomerKycMappingDTO> kycDocuments;

    private LocalDateTime createdAt;
}