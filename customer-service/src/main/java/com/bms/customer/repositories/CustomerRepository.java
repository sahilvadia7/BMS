package com.bms.customer.repositories;

import com.bms.customer.entities.Customer;
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

    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.kycDocuments m LEFT JOIN FETCH m.kyc WHERE c.customerId = :id")
    Optional<Customer> findWithKycByCustomerId(@Param("id") Long id);

    @Query("""
                SELECT c FROM Customer c
                JOIN CustomerKycMapping m ON m.customer.customerId = c.customerId
                JOIN Kyc k ON k.id = m.kyc.id
                WHERE k.documentType = :docType AND k.documentNumber = :docNumber
            """)
    Optional<Customer> findCustomerByKycDocument(
            @Param("docType") String docType,
            @Param("docNumber") String docNumber);

    @Query("""
                SELECT c FROM Customer c
                JOIN CustomerKycMapping m ON m.customer.customerId = c.customerId
                WHERE m.kyc.id = :kycId
            """)
    Optional<Customer> findCustomerByKycId(@Param("kycId") Long kycId);

    @Query("SELECT c FROM Customer c WHERE c.cifNumber = :cifId AND c.email = :email")
    Optional<Customer> findByCifNumberAndEmail(@Param("cifId") String cifId, @Param("email") String email);

}
