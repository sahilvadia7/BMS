package com.bms.loan.service.impl;

import com.bms.loan.Repository.LoanEmiScheduleRepository;
import com.bms.loan.Repository.LoanRepository;
import com.bms.loan.entity.loan.LoanEmiSchedule;
import com.bms.loan.entity.loan.Loans;
import com.bms.loan.enums.EmiStatus;
import com.bms.loan.enums.LoanStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmiOverdueScheduler {

    private final LoanEmiScheduleRepository emiRepository;
    private final LoanRepository loanRepository;

    // Runs every day at 1 AM
    @Scheduled(cron = "0 0 1 * * *")
    public void markOverdueEmis() {
        LocalDate today = LocalDate.now();
        List<LoanEmiSchedule> overdueEmis = emiRepository.findAllOverdueEmis(today);

        if (overdueEmis.isEmpty()) {
            log.info("No overdue EMIs found today ({})", today);
            return;
        }

        log.info("Found {} overdue EMIs on {}", overdueEmis.size(), today);

        for (LoanEmiSchedule emi : overdueEmis) {
            emi.setStatus(EmiStatus.OVERDUE);
            emi.setLateFee(calculateLateFee(emi.getEmiAmount(), emi.getDueDate(), today));
            emiRepository.save(emi);

            // update loan status
            Loans loan = emi.getLoan();
            if (loan.getStatus() != LoanStatus.CLOSED) {
                loan.setStatus(LoanStatus.OVERDUE);
                loanRepository.save(loan);
            }
        }
    }

    private BigDecimal calculateLateFee(BigDecimal emiAmount, LocalDate due, LocalDate today) {
        long daysLate = ChronoUnit.DAYS.between(due, today);
        BigDecimal dailyRate = new BigDecimal("0.01"); // 1% per day
        return emiAmount.multiply(dailyRate)
                .multiply(BigDecimal.valueOf(daysLate))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
