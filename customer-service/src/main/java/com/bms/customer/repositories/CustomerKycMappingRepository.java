package com.bms.customer.repositories;


import com.bms.customer.entities.CustomerKycMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerKycMappingRepository extends JpaRepository<CustomerKycMapping, Long> {
    // Custom query methods here
}