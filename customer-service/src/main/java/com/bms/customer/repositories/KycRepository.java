package com.bms.customer.repositories;

import com.bms.customer.entities.CustomerKycMapping;
import com.bms.customer.entities.Kyc;
import feign.Param;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KycRepository extends JpaRepository<Kyc,Long> {
    Optional<Kyc> findByDocumentTypeAndDocumentNumber(@NotBlank(message = "Document type is required") String s, @NotBlank(message = "Document number is required") String s1);
    Optional<Kyc> findByDocumentNumber(String documentNumber);


//    @Query("SELECT k.id FROM Kyc k WHERE k.customer.customerId = :customerId")
//    Optional<Long> findKycIdByCustomer_CustomerId(@Param("customerId") Long customerId);

}
