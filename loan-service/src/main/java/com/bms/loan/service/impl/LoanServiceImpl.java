package com.bms.loan.service.impl;

import com.bms.loan.Repository.LoanApplicationRepository;
import com.bms.loan.Repository.RepaymentRepository;
import com.bms.loan.Repository.RepaymentScheduleRepository;
import com.bms.loan.dto.request.ApproveLoanRequestDto;
import com.bms.loan.dto.request.DisburseLoanRequestDto;
import com.bms.loan.dto.request.LoanApplicationDto;
import com.bms.loan.dto.request.RepaymentRequestDto;
import com.bms.loan.dto.response.*;
import com.bms.loan.entity.LoanApplication;
import com.bms.loan.entity.Repayment;
import com.bms.loan.entity.RepaymentSchedule;
import com.bms.loan.enums.LoanStatus;
import com.bms.loan.enums.PaymentStatus;
import com.bms.loan.feign.AccountClient;
import com.bms.loan.feign.UserClient;
import com.bms.loan.service.LoanService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoanServiceImpl implements LoanService {

    private final LoanApplicationRepository loanRepo;
    private final RepaymentScheduleRepository scheduleRepo;
    private final RepaymentRepository repaymentRepo;
    private final UserClient userClient;
    private final LoanMapper loanMapper;
    private final AccountClient accountClient;


    public LoanServiceImpl(LoanApplicationRepository loanRepo, RepaymentScheduleRepository scheduleRepo, RepaymentRepository repaymentRepo, UserClient userClient, LoanMapper mapper, AccountClient accountClient) {
        this.loanRepo = loanRepo;
        this.scheduleRepo = scheduleRepo;
        this.repaymentRepo = repaymentRepo;
        this.userClient = userClient;
        this.loanMapper = mapper;
        this.accountClient = accountClient;
    }


    @Override
    public LoanApplicationResponse applyLoan(LoanApplicationDto dto) {

        UserResponseDto user = userClient.getUserById(dto.getCustomerId());
        if (user == null || !user.getIsActive()) {
            throw new RuntimeException("User not found or inactive");
        }

//        AccountResponseDTO account = accountClient.getAccountById(dto.)

        LoanApplication loan = loanMapper.toEntity(dto, user.getUserId());
        LoanApplication saved = loanRepo.save(loan);

        return loanMapper.toResponse(saved);
    }

    @Override
    public LoanEvaluationResponse evaluateLoan(Long loanId, Integer creditScore, BigDecimal monthlyIncome, BigDecimal existingEmi) {
        LoanApplication loan = loanRepo.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!loan.getStatus().equals(LoanStatus.APPLIED)) {
            throw new RuntimeException("Loan not in APPLIED status");
        }

        // Simple eligibility logic: EMI should not exceed 50% of income
        BigDecimal emi = calculateEMI(loan.getPrincipal(), loan.getAnnualRate(), loan.getTenureMonths());
        BigDecimal maxEligibleEmi = monthlyIncome.multiply(BigDecimal.valueOf(0.5));

        String message;
        if (emi.compareTo(maxEligibleEmi) > 0 || creditScore < 650) {
            loan.setStatus(LoanStatus.REJECTED);
            message = "Loan rejected due to eligibility criteria";

        } else {
            loan.setStatus(LoanStatus.UNDER_REVIEW);
            message = "Loan moved to UNDER_REVIEW";
        }

        loan.setCreditScore(creditScore);
        loanRepo.save(loan);

        return LoanEvaluationResponse.builder()
                .loanId(loan.getId())
                .status(loan.getStatus())
                .creditScore(loan.getCreditScore())
                .principal(loan.getPrincipal())
                .tenureMonths(BigDecimal.valueOf(loan.getTenureMonths()))
                .annualRate(loan.getAnnualRate())
                .calculatedEmi(emi)
                .maxEligibleEmi(maxEligibleEmi)
                .message(message)
                .build();
    }

    @Override
    public LoanApprovalResponse approveLoan(Long loanId, ApproveLoanRequestDto dto) {
        LoanApplication loan = loanRepo.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!loan.getStatus().equals(LoanStatus.UNDER_REVIEW)) {
            throw new RuntimeException("Loan not in UNDER_REVIEW status");
        }

        loan.setPrincipal(dto.getApprovedAmount());
        loan.setAnnualRate(dto.getApprovedRate());
        loan.setTenureMonths(dto.getApprovedTenure());
        loan.setApprovedBy(dto.getApprovedBy());
        loan.setApprovedAt(LocalDateTime.now());
        loan.setStatus(LoanStatus.APPROVED);
        loan.setOutstandingBalance(dto.getApprovedAmount());

        loanRepo.save(loan);

        return LoanApprovalResponse.builder()
                .loanId(loan.getId())
                .status(loan.getStatus())
                .approvedAmount(loan.getPrincipal())
                .approvedRate(loan.getAnnualRate())
                .approvedTenure(loan.getTenureMonths())
                .approvedBy(loan.getApprovedBy())
                .approvedAt(loan.getApprovedAt())
                .message("Loan approved successfully")
                .build();
    }

    @Override
    public LoanRejectionResponseDto rejectLoan(Long loanId, String reason) {
        LoanApplication loan = loanRepo.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        loan.setStatus(LoanStatus.REJECTED);
        loanRepo.save(loan);
        // optionally store reason somewhere
        return LoanRejectionResponseDto.builder()
                .id(loan.getId())
                .customerId(loan.getCustomerId())
                .accountNumber(loan.getAccountNumber())
                .productCode(loan.getProductCode())
                .principal(loan.getPrincipal())
                .tenureMonths(loan.getTenureMonths())
                .annualRate(loan.getAnnualRate())
                .status(loan.getStatus())
                .rejectedReason(reason)
                .appliedAt(loan.getAppliedAt())
                .rejectedAt(LocalDateTime.now())
                .message("Loan rejected successfully")
                .build();
    }

    @Override
    public LoanDisbursementResponseDto disburseLoan(Long loanId, DisburseLoanRequestDto dto) {
        LoanApplication loan = loanRepo.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!loan.getStatus().equals(LoanStatus.APPROVED)) {
            throw new RuntimeException("Loan not in APPROVED status");
        }

        loan.setDisbursedAt(dto.getDisbursedDate() != null ? dto.getDisbursedDate() : LocalDateTime.now());
        loan.setOutstandingBalance(loan.getPrincipal());
        loan.setStatus(LoanStatus.DISBURSED);

        // generate EMI schedule
        generateRepaymentSchedule(loan);

        LoanApplication savedLoan = loanRepo.save(loan);
        return loanMapper.toDisbursementResponse(savedLoan);
    }

    @Override
    public List<RepaymentScheduleResponseDto> getRepaymentSchedule(Long loanId) {
        List<RepaymentSchedule> repaymentScheduleList = scheduleRepo.findByLoanApplicationId(loanId);

        return repaymentScheduleList.stream()
                .map(LoanMapper::toRepaymentScheduleResponseDto)
                .toList();
    }

    @Override
    public RepaymentResponseDto repayLoan(Long loanId, RepaymentRequestDto dto) {
        LoanApplication loan = loanRepo.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!loan.getStatus().equals(LoanStatus.DISBURSED)) {
            throw new RuntimeException("Loan not disbursed");
        }

        // find next pending EMI
        RepaymentSchedule schedule = scheduleRepo.findFirstByLoanApplicationIdAndStatusOrderByDueDateAsc(loanId, PaymentStatus.PENDING)
                .orElseThrow(() -> new RuntimeException("No pending EMI"));

        schedule.setPaidAmount(schedule.getPaidAmount().add(dto.getAmount()));

        // update status
        if (schedule.getPaidAmount().compareTo(schedule.getEmi()) >= 0) {
            schedule.setStatus(PaymentStatus.PAID);
        } else {
            schedule.setStatus(PaymentStatus.PARTIAL);
        }

        // reduce outstanding balance
        loan.setOutstandingBalance(loan.getOutstandingBalance().subtract(dto.getAmount()));

        // save repayment
        Repayment repayment = Repayment.builder()
                .loanApplication(loan)
                .schedule(schedule)
                .amount(dto.getAmount())
                .paymentMode(dto.getPaymentMode())
                .txnRef(dto.getTxnRef())
                .paymentDate(LocalDateTime.now())
                .build();

        repaymentRepo.save(repayment);
        scheduleRepo.save(schedule);

        // if balance 0 → close loan automatically
        if (loan.getOutstandingBalance().compareTo(BigDecimal.ZERO) <= 0) {
            loan.setStatus(LoanStatus.CLOSED);
        }
        loanRepo.save(loan);

        return RepaymentResponseDto.builder()
                .loanId(loan.getId())
                .paidAmount(dto.getAmount())
                .outstandingBalance(loan.getOutstandingBalance())
                .emiStatus(schedule.getStatus())
                .paymentMode(dto.getPaymentMode())
                .txnRef(dto.getTxnRef())
                .paymentDate(repayment.getPaymentDate())
                .message("Repayment successful")
                .build();
    }

    @Override
    public void markOverdue() {
        LocalDate today = LocalDate.now();
        List<RepaymentSchedule> schedules = scheduleRepo.findByStatusAndDueDateBefore(PaymentStatus.PENDING, today);
        for (RepaymentSchedule schedule : schedules) {
            schedule.setStatus(PaymentStatus.OVERDUE);
        }
        scheduleRepo.saveAll(schedules);
    }

    @Override
    public LoanCloseResponseDto closeLoan(Long loanId) {
        LoanApplication loan = loanRepo.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        loan.setStatus(LoanStatus.CLOSED);
        loan.setOutstandingBalance(BigDecimal.ZERO);

        LoanApplication savedLoan = loanRepo.save(loan);

        return LoanCloseResponseDto.builder()
                .loanId(savedLoan.getId())
                .customerId(savedLoan.getCustomerId())
                .productCode(savedLoan.getProductCode().name())
                .status(savedLoan.getStatus().name())
                .outstandingBalance(savedLoan.getOutstandingBalance())
                .message("Loan closed successfully")
                .build();
    }

    // ===== Helper Methods =====

    private BigDecimal calculateEMI(BigDecimal principal, BigDecimal rate, int tenureMonths) {
        // EMI = [P x R x (1+R)^N]/[(1+R)^N-1], R = monthly interest rate
        BigDecimal monthlyRate = rate.divide(BigDecimal.valueOf(12 * 100), 10, BigDecimal.ROUND_HALF_UP);
        BigDecimal numerator = principal.multiply(monthlyRate).multiply((BigDecimal.ONE.add(monthlyRate)).pow(tenureMonths));
        BigDecimal denominator = (BigDecimal.ONE.add(monthlyRate)).pow(tenureMonths).subtract(BigDecimal.ONE);
        return numerator.divide(denominator, 2, BigDecimal.ROUND_HALF_UP);
    }

    private void generateRepaymentSchedule(LoanApplication loan) {
        BigDecimal emi = calculateEMI(loan.getPrincipal(), loan.getAnnualRate(), loan.getTenureMonths());
        LocalDate dueDate = LocalDate.now().plusMonths(1);

        for (int i = 0; i < loan.getTenureMonths(); i++) {
            RepaymentSchedule schedule = new RepaymentSchedule();
            schedule.setLoanApplication(loan);
            schedule.setDueDate(dueDate);
            schedule.setEmi(emi);
            schedule.setPrincipalDue(loan.getPrincipal().divide(BigDecimal.valueOf(loan.getTenureMonths()), 2, BigDecimal.ROUND_HALF_UP));
            schedule.setInterestDue(emi.subtract(schedule.getPrincipalDue()));
            schedule.setStatus(PaymentStatus.PENDING);

            scheduleRepo.save(schedule);
            dueDate = dueDate.plusMonths(1);
        }
    }
}
