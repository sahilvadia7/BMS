package com.bms.customer.repositories;

import com.bms.customer.entities.Customer;
import com.bms.customer.entities.CustomerOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerOtpRepository extends JpaRepository<CustomerOtp, Long> {
    Optional<CustomerOtp> findTopByCustomerOrderByCreatedAtDesc(Customer customer);
}

