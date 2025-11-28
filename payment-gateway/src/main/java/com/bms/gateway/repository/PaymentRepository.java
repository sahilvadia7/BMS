package com.bms.gateway.repository;

import com.bms.gateway.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

	Optional<Payment> findByExternalReferenceId(String externalReferenceId);
}
