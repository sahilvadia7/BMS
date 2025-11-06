package com.bms.creditscore.dto.extrenal;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class CustomerFeignDTO {
    private Long customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNo;
    private String role;
    private String status;
    private String gender;
    private String cifNumber;
    private String address;
    private LocalDate dob;
    private Set<Object> kycDocuments;
    private LocalDateTime createdAt;
}
