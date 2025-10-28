package com.bms.loan.Repository;

import com.bms.loan.entity.loan.LoanEmiSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanEmiScheduleRepository extends JpaRepository<LoanEmiSchedule, Long> {
    List<LoanEmiSchedule> findByLoan_LoanId(Long loanId);

    Optional<LoanEmiSchedule> findByIdAndLoan_LoanId(Long emiId, Long loanId);

    @Query("SELECT e FROM LoanEmiSchedule e WHERE e.status = 'UNPAID' AND e.dueDate < :today")
    List<LoanEmiSchedule> findAllOverdueEmis(@Param("today") LocalDate today);

}
