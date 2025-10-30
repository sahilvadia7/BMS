package com.bms.customer.repositories;

import com.bms.customer.entities.CustomerKycMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CustomerKycMappingRepository extends JpaRepository<CustomerKycMapping, Long> {

    @Modifying
    @Query("DELETE FROM CustomerKycMapping m WHERE m.customer.customerId = :customerId")
    void deleteByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT COUNT(m) > 0 FROM CustomerKycMapping m WHERE m.customer.customerId = :customerId")
    boolean existsByCustomerId(@Param("customerId") Long customerId);

    Optional<CustomerKycMapping> findByCustomer_CustomerId(Long customerId);

    @Query("SELECT m.kyc.id FROM CustomerKycMapping m WHERE m.customer.customerId = :customerId")
    Optional<Long> findKycIdByCustomerId(@Param("customerId") Long customerId);

}