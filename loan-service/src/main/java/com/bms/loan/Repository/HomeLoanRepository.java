package com.bms.loan.Repository;

import com.bms.loan.entity.HomeLoanDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HomeLoanRepository extends JpaRepository<HomeLoanDetails, Long> {
}
