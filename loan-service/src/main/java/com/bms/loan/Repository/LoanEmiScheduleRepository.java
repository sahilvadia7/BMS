package com.bms.loan.Repository;

import com.bms.loan.entity.LoanEmiSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanEmiScheduleRepository extends JpaRepository<LoanEmiSchedule, Long> {
}
