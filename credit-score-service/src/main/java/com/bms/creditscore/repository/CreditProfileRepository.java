package com.bms.creditscore.repository;

import com.bms.creditscore.model.CreditProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

import java.util.List;
import java.util.Optional;

@Repository
public interface CreditProfileRepository extends JpaRepository<CreditProfile, UUID> {
    Optional<CreditProfile> findFirstByCustomerIdOrderByCalculatedAtDesc(Long customerId);
    List<CreditProfile> findByCustomerIdOrderByCalculatedAtDesc(Long customerId);
}
