package com.bms.customer.repositories;

import com.bms.customer.entities.Customer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByPhoneNo(String phoneNo);

    Optional<Customer> findByCifNumber(String cifNumber);

    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.kycDocuments WHERE c.customerId = :id")
    Optional<Customer> findWithKycByCustomerId(@Param("id") Long id);
}
