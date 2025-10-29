package com.bms.loan.dto.response;

import com.bms.loan.enums.Gender;
import com.bms.loan.enums.Roles;
import com.bms.loan.enums.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
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

    private Set<CustomerKycMappingDTO> kycDocuments;
    private LocalDateTime createdAt;
}
