package com.bms.loan.Repository;

import com.bms.loan.entity.RepaymentSchedule;
import com.bms.loan.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepaymentScheduleRepository extends JpaRepository<RepaymentSchedule , Long> {
    Optional<RepaymentSchedule> findFirstByLoanApplicationIdAndStatusOrderByDueDateAsc(Long loanId, PaymentStatus paymentStatus);

    List<RepaymentSchedule> findByStatusAndDueDateBefore(PaymentStatus status, java.time.LocalDate date);

    List<RepaymentSchedule> findByLoanApplicationId(Long loanId);
}
