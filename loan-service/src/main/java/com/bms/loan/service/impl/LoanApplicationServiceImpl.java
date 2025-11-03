package com.bms.loan.service.impl;

import com.bms.loan.Repository.*;
import com.bms.loan.Repository.education.EducationLoanRepository;
import com.bms.loan.Repository.education.EducationVerificationReportRepository;
import com.bms.loan.Repository.home.HomeLoanRepository;
import com.bms.loan.dto.email.ApplyLoanEmailDTO;
import com.bms.loan.dto.email.DisbursementEmailDTO;
import com.bms.loan.dto.request.*;
import com.bms.loan.dto.request.car.CarLoanDetailsDto;
import com.bms.loan.dto.request.car.CarLoanEvaluationRequestDto;
import com.bms.loan.dto.request.education.EducationLoanDetailsDto;
import com.bms.loan.dto.request.education.EducationVerificationRequestDto;
import com.bms.loan.dto.request.home.HomeLoanDetailsDto;
import com.bms.loan.dto.response.*;
import com.bms.loan.dto.response.car.CarLoanEvaluationByBankResponse;
import com.bms.loan.dto.response.education.EducationEvaluationResponse;
import com.bms.loan.dto.response.emi.EmiSummary;
import com.bms.loan.dto.response.emi.LoanEmiScheduleResponse;
import com.bms.loan.dto.response.loan.*;
import com.bms.loan.entity.*;
import com.bms.loan.entity.car.CarLoanDetails;
import com.bms.loan.entity.education.EducationLoanDetails;
import com.bms.loan.entity.education.EducationVerificationReport;
import com.bms.loan.entity.home.HomeLoanDetails;
import com.bms.loan.entity.loan.LoanEmiSchedule;
import com.bms.loan.entity.loan.LoanPrepayment;
import com.bms.loan.entity.loan.Loans;
import com.bms.loan.enums.EmiStatus;
import com.bms.loan.enums.LoanStatus;
import com.bms.loan.enums.PrepaymentOption;
import com.bms.loan.exception.InvalidLoanStatusException;
import com.bms.loan.exception.ResourceNotFoundException;
import com.bms.loan.feign.CustomerClient;
import com.bms.loan.feign.NotificationClient;
import com.bms.loan.service.EducationLoanService;
import com.bms.loan.service.HomeLoanService;
import com.bms.loan.service.LoanApplicationService;
import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Primary
public class LoanApplicationServiceImpl implements LoanApplicationService {

    private final LoanRepository loansRepository;
    private final CarLoanRepository carLoanRepo;
    private final HomeLoanRepository homeLoanRepo;
    private final EducationLoanRepository educationLoanRepo;
    private final LoanEmiScheduleRepository loanEmiScheduleRepository;
    private final InterestRateRepository interestRateRepository;
    private final EducationVerificationReportRepository educationVerificationRepository;
    private final LoanPrepaymentRepository loanPrepaymentRepo;

    private final HomeLoanService homeLoanService;
    private final EducationLoanService educationLoanService;
    private final CarLoanEvaluator carLoanEvaluator;
    private final CustomerClient customerClient;
    private final NotificationClient notificationClient;
    private final Mapper mapper;

    @Override
    public LoanApplicationResponse applyLoan(LoanApplicationRequest request) {

        InterestRate interestRate = interestRateRepository.findByLoanType(String.valueOf(request.getLoanType()))
                .orElseThrow(() -> new ResourceNotFoundException("Interest Rate not found with Type: " + request.getLoanType()));

        if (interestRate == null) {
            throw new ResourceNotFoundException("Interest rate not found with id: ");
        }
        if (request.getRequestedTenureMonths() > interestRate.getMaxTenure()){
            throw new RuntimeException("Tenure months cannot be greater than interest rate maximum");
        }

        CustomerResponseDTO customer = new CustomerResponseDTO();
        try {
            if(request.getCifNumber()==null || request.getCifNumber().isEmpty()){
                request.setCifNumber("0000");
            }

            CustomerDetailsResponseDTO customerDetails = customerClient.getByCif(request.getCifNumber());
            customer.setCId(customerDetails.getCustomerId());
            customer.setCifNumber(customerDetails.getCifNumber());

        } catch (FeignException e) {
            if (e.status() == 404) {
                // Call register customer
                customer = customerClient.registerCustomer(request.getCustomerDetails());
            } else {
                // Other HTTP errors (500, etc.)
                throw e;
            }
        }

        // Create main loan record
        Loans loan = Loans.builder()
                .customerId(customer.getCId())
                .cifNumber(customer.getCifNumber())
                .loanType(request.getLoanType())
                .interestRate(interestRate.getBaseRate())
                .requestedAmount(request.getRequestedAmount())
                .requestedTenureMonths(request.getRequestedTenureMonths())
                .totalAmountPaid(BigDecimal.valueOf(0))
                .employmentType(request.getEmploymentType())
                .monthlyIncome(request.getMonthlyIncome())
                .bankName(request.getBankName())
                .bankAccount(request.getBankAccount())
                .ifscCode(request.getIfscCode())
                .status(LoanStatus.APPLIED)
                .build();

        Loans savedLoan = loansRepository.save(loan);

        // Save specific details based on loan type
        switch (request.getLoanType()) {
            case CAR -> {
                CarLoanDetailsDto cd = request.getCarDetails();

                int currentYear = Year.now().getValue();

                carLoanRepo.save(CarLoanDetails.builder()
                        .loans(savedLoan)
                        .carModel(cd.getCarModel())
                        .manufacturer(cd.getManufacturer())
                        .manufactureYear(cd.getManufactureYear())
                        .carValue(cd.getCarValue())
                        .registrationNumber(cd.getRegistrationNumber())
                        .carAgeYears(currentYear - cd.getManufactureYear())
                        .downPayment(cd.getDownPayment())
                        .carConditionScore(-1)
                        .insuranceValid(false)
                        .employmentStabilityYears(65)
                        .build());
            }
            case HOME -> {
                HomeLoanDetailsDto hd = request.getHomeDetails();
                homeLoanRepo.save(HomeLoanDetails.builder()
                        .loans(savedLoan)
                        .propertyAddress(hd.getPropertyAddress())
                        .propertyValue(hd.getPropertyValue())
                        .builderName(hd.getBuilderName())
                        .downPayment(hd.getDownPayment())
                        .propertyType(hd.getPropertyType())
                        .ownershipType(hd.getOwnershipType())
                        .registrationNumber(hd.getRegistrationNumber())
                        .approvedByAuthority(false).build());
            }
            case EDUCATION -> {
                EducationLoanDetailsDto ed = request.getEducationDetails();
                // Calculate total course cost
                BigDecimal totalCourseCost = BigDecimal.ZERO;
                if (ed.getTuitionFees() != null) totalCourseCost = totalCourseCost.add(ed.getTuitionFees());
                if (ed.getLivingExpenses() != null) totalCourseCost = totalCourseCost.add(ed.getLivingExpenses());
                if (ed.getOtherExpenses() != null) totalCourseCost = totalCourseCost.add(ed.getOtherExpenses());

                educationLoanRepo.save(EducationLoanDetails.builder()
                        .loans(savedLoan)
                        .courseName(ed.getCourseName())
                        .fieldOfStudy(ed.getFieldOfStudy())
                        .university(ed.getUniversity())
                        .country(ed.getCountry())
                        .courseDurationMonths(ed.getCourseDurationMonths())
                        .courseStartDate(ed.getCourseStartDate())
                        .expectedCompletionDate(ed.getExpectedCompletionDate())
                        .tuitionFees(ed.getTuitionFees())
                        .livingExpenses(ed.getLivingExpenses())
                        .otherExpenses(ed.getOtherExpenses())
                        .totalCourseCost(totalCourseCost)
                        .coApplicantName(ed.getCoApplicantName())
                        .coApplicantRelation(ed.getCoApplicantRelation())
                        .coApplicantOccupation(ed.getCoApplicantOccupation())
                        .coApplicantAnnualIncome(ed.getCoApplicantAnnualIncome())
                        .moratoriumMonths(ed.getMoratoriumMonths())
                        .build());
            }
            default -> throw new IllegalArgumentException("Unsupported loan type");
        }

        ApplyLoanEmailDTO applyLoanEmailDTO = ApplyLoanEmailDTO.builder()
                .email(request.getCustomerDetails().getEmail())
                .customerName(request.getCustomerDetails().getFirstName()+" "+request.getCustomerDetails().getLastName())
                .loanId(savedLoan.getLoanId())
                .cifNumber(savedLoan.getCifNumber())
                .build();

        // Send notification email
        notificationClient.sendApplyLoanEmail(applyLoanEmailDTO);

        //Build and return response
        return LoanApplicationResponse.builder()
                .loanId(savedLoan.getLoanId())
                .cifNumber(customer.getCifNumber())
                .loanType(String.valueOf(savedLoan.getLoanType()))
                .status(String.valueOf(savedLoan.getStatus()))
                .message("Loan application submitted successfully.")
                .build();
    }

    @Override
    public CarLoanEvaluationByBankResponse updateEvaluationData(Long loanId, CarLoanEvaluationRequestDto request) {

        Loans loan = loansRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found for ID: " + loanId));

        if (loan.getStatus() != LoanStatus.APPLIED) {
            throw new InvalidLoanStatusException("Loan status is not verified");
        }

        CarLoanDetails carLoan = carLoanRepo.findByLoans_LoanId(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Car loan not found for loanId: " + loanId));

        if (request.getDownPayment() != null)
            carLoan.setDownPayment(request.getDownPayment());

        carLoan.setInsuranceValid(request.isInsuranceValid());
        carLoan.setCarConditionScore(request.getCarConditionScore());
        carLoan.setEmploymentStabilityYears(request.getEmploymentStabilityYears());

        CarLoanDetails saved = carLoanRepo.save(carLoan);

        // Make sure loans object is loaded
        Long loanIdValue = saved.getLoans().getLoanId();

        loan.setStatus(LoanStatus.VERIFIED);
        loansRepository.save(loan);

        return CarLoanEvaluationByBankResponse.builder()
                .loanId(loanIdValue)
                .carModel(saved.getCarModel())
                .manufacturer(saved.getManufacturer())
                .manufactureYear(saved.getManufactureYear())
                .carValue(saved.getCarValue())
                .registrationNumber(saved.getRegistrationNumber())
                .carAgeYears(saved.getCarAgeYears())
                .downPayment(saved.getDownPayment())
                .insuranceValid(saved.isInsuranceValid())
                .carConditionScore(saved.getCarConditionScore())
                .employmentStabilityYears(saved.getEmploymentStabilityYears())
                .build();
    }

    @Override
    public EducationEvaluationResponse verifyEducationBackground(Long loanId, EducationVerificationRequestDto request) {
        Loans loan = loansRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found for ID: " + loanId));

        if (loan.getStatus() != LoanStatus.APPLIED) {
            throw new InvalidLoanStatusException("Loan status is not verified");
        }

        EducationVerificationReport report = educationVerificationRepository
                .findByLoans_LoanId(loanId)
                .orElse(EducationVerificationReport.builder().loans(loan).build());

        report.setAdmissionVerified(request.isAdmissionVerified());
        report.setCollegeRecognized(request.isCollegeRecognized());
        report.setFeeStructureVerified(request.isFeeStructureVerified());
        report.setOfficerName(request.getOfficerName());
        report.setOfficerRemarks(request.getOfficerRemarks());
        report.setVerificationDate(request.getVerificationDate());


        boolean verified = request.isAdmissionVerified()
                && request.isCollegeRecognized()
                && request.isFeeStructureVerified();

        report.setVerifiedSuccessfully(verified);
        educationVerificationRepository.save(report);

        // Update EducationLoanDetails (mark verified)
        EducationLoanDetails details = educationLoanRepo.findByLoans_LoanId(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("EducationLoanDetails not found for Loan ID: " + loanId));
        details.setVerified(true);
        educationLoanRepo.save(details);

        // Update Loan Status
        loan.setStatus(verified ? LoanStatus.VERIFIED : LoanStatus.VERIFICATION_PENDING);
        loansRepository.save(loan);

        return EducationEvaluationResponse.builder()
                .loanId(loan.getLoanId())
                .verifiedSuccessfully(true)
                .officerName(report.getOfficerName())
                .remarks(report.getOfficerRemarks())
                .verificationDate(report.getVerificationDate())
                .status(loan.getStatus().name())
                .message(verified ?
                        "Education loan verification completed successfully." :
                        "Verification incomplete. Some details need rechecking.")
                .build();
    }

    @Override
    public LoanEvaluationResponse evaluateLoan(Long loanId) {
        Loans loan = loansRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + loanId));

        if (loan.getStatus() != LoanStatus.VERIFIED) {
            throw new InvalidLoanStatusException("Loan status is not verified");
        }

        LoanEvaluationResponse loanEvaluationResponse = new LoanEvaluationResponse();
        loanEvaluationResponse.setLoanId(loanId);

        switch (loan.getLoanType()) {
            case CAR -> {
                LoanEvaluationResult carResult = carLoanEvaluator.evaluateCarLoan(loanId);
                loanEvaluationResponse.setLoanType(String.valueOf(loan.getLoanType()));
                loanEvaluationResponse.setApproved(carResult.isApproved());
                loanEvaluationResponse.setRemarks(carResult.getRemarks());
                loanEvaluationResponse.setStatus(String.valueOf(carResult.getStatus()));
            }
            case HOME -> {
                // Delegate home loan evaluation to HomeLoanService
                LoanEvaluationResponse homeResult = homeLoanService.evaluateLoan(loanId);
                loanEvaluationResponse.setLoanType(homeResult.getLoanType());
                loanEvaluationResponse.setApproved(homeResult.isApproved());
                loanEvaluationResponse.setRemarks(homeResult.getRemarks());
                loanEvaluationResponse.setStatus(homeResult.getStatus());

            }
            case EDUCATION -> {

                LoanEvaluationResponse educationResult = educationLoanService.evaluateLoan(loanId);
                loanEvaluationResponse.setLoanType(educationResult.getLoanType());
                loanEvaluationResponse.setApproved(educationResult.isApproved());
                loanEvaluationResponse.setRemarks(educationResult.getRemarks());
                loanEvaluationResponse.setStatus(educationResult.getStatus());
            }

            default -> throw new IllegalArgumentException("Unsupported loan type: " + loan.getLoanType());
        }

        // Return unified response
        return loanEvaluationResponse;
    }



    public LoanDisbursementResponse disburseLoan(Long loanId) {
        Loans loan = loansRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (!loan.isESign()) {
            throw new IllegalStateException("eSign is not completed");
        }

        if (loan.getStatus() != LoanStatus.SANCTIONED) {
            throw new IllegalStateException("Loan is not sanctioned by eSign for disbursement");
        }

        // Update loan status and dates
        loan.setStatus(LoanStatus.DISBURSED);
        loan.setOutstandingAmount(loan.getApprovedAmount());
        loan.setDisbursementDate(LocalDate.now());

        loansRepository.save(loan);

        // Calculate EMI
        BigDecimal emi = calculateEmi(
                loan.getApprovedAmount(),
                loan.getInterestRate(),
                loan.getRequestedTenureMonths()
        );

        BigDecimal remainingBalance = loan.getApprovedAmount();
        BigDecimal monthlyRate = loan.getInterestRate()
                .divide(BigDecimal.valueOf(12 * 100), 10, RoundingMode.HALF_UP);

        LocalDate dueDate = LocalDate.now().plusMonths(1);

        for (int i = 1; i <= loan.getRequestedTenureMonths(); i++) {

            // interest = remaining balance × monthly rate
            BigDecimal interestComponent = remainingBalance.multiply(monthlyRate)
                    .setScale(2, RoundingMode.HALF_UP);

            // principal = emi − interest
            BigDecimal principalComponent = emi.subtract(interestComponent)
                    .setScale(2, RoundingMode.HALF_UP);

            // update remaining balance
            remainingBalance = remainingBalance.subtract(principalComponent)
                    .setScale(2, RoundingMode.HALF_UP);

            // Adjust rounding on last month
            if (i == loan.getRequestedTenureMonths() && remainingBalance.compareTo(BigDecimal.ZERO) != 0) {
                principalComponent = principalComponent.add(remainingBalance);
                remainingBalance = BigDecimal.ZERO;
            }

            // Save EMI schedule
            LoanEmiSchedule schedule = LoanEmiSchedule.builder()
                    .loan(loan)
                    .installmentNumber(i)
                    .dueDate(dueDate)
                    .emiAmount(emi)
                    .principalComponent(principalComponent)
                    .interestComponent(interestComponent)
                    .status(EmiStatus.UNPAID)
                    .build();

            loanEmiScheduleRepository.save(schedule);
            dueDate = dueDate.plusMonths(1);

        }

        // Fetch first 3 EMIs for email preview
        List<LoanEmiSchedule> firstFewEmis = loanEmiScheduleRepository
                .findTop3ByLoanOrderByInstallmentNumberAsc(loan);

        List<EmiSummary> emiPreview = firstFewEmis.stream()
                .map(e -> EmiSummary.builder()
                        .installmentNumber(e.getInstallmentNumber())
                        .dueDate(e.getDueDate())
                        .emiAmount(e.getEmiAmount())
                        .principalComponent(e.getPrincipalComponent())
                        .interestComponent(e.getInterestComponent())
                        .build())
                .toList();

        // Fetch customer details
        CustomerDetailsResponseDTO customer = customerClient.getByCif(loan.getCifNumber());

        // Prepare email
        DisbursementEmailDTO emailDTO = DisbursementEmailDTO.builder()
                .toEmail(customer.getEmail())
                .customerName(customer.getFirstName() + " " + customer.getLastName())
                .loanType(loan.getLoanType().name())
                .sanctionedAmount(loan.getApprovedAmount())
                .interestRate(loan.getInterestRate())
                .tenureMonths(loan.getRequestedTenureMonths())
                .emiAmount(emi)
                .firstEmiDate(LocalDate.now().plusMonths(1))
                .firstFewEmis(emiPreview)
                .build();

        notificationClient.sendDisbursementEmail(emailDTO);

        return LoanDisbursementResponse.builder()
                .loanId(loan.getLoanId())
                .status(String.valueOf(loan.getStatus()))
                .emi(emi)
                .message("Loan disbursed successfully and EMI schedule created")
                .build();
    }


    @Override
    public List<LoanEmiScheduleResponse> getEmiSchedule(Long loanId) {
        List<LoanEmiSchedule> emis = loanEmiScheduleRepository.findByLoan_LoanId(loanId);
        return emis.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    @Transactional
    @Override
    public void payEmi(Long loanId, Long emiId, LocalDate paymentDate) {
        LoanEmiSchedule emi = loanEmiScheduleRepository.findByIdAndLoan_LoanId(emiId, loanId)
                .orElseThrow(() -> new ResourceNotFoundException("EMI not found for given loan"));

        Loans loan = emi.getLoan();

        if (emi.getStatus() == EmiStatus.PAID || emi.getStatus() == EmiStatus.LATE) {
            throw new IllegalStateException("This EMI is already paid");
        }

        BigDecimal lateFee = BigDecimal.ZERO;
        long daysLate = 0;

        if (paymentDate.isAfter(emi.getDueDate())) {
            emi.setStatus(EmiStatus.LATE);
            daysLate = ChronoUnit.DAYS.between(emi.getDueDate(), paymentDate);
            lateFee = calculateLateFee(emi.getEmiAmount(), daysLate);
            emi.setLateFee(lateFee);
            emi.setDaysLate((int) daysLate);
            emi.setRemarks("Paid late by " + daysLate + " days");
        } else {
            emi.setStatus(EmiStatus.PAID);
            emi.setLateFee(BigDecimal.ZERO);
        }

        // total amount paid (EMI + penalty)
        BigDecimal totalPayment = emi.getEmiAmount().add(lateFee);
        emi.setPaymentDate(paymentDate);
        emi.setPaidAmount(totalPayment);

        loanEmiScheduleRepository.save(emi);

        // Update loan summary
        BigDecimal newTotalPaid = loan.getTotalAmountPaid().add(totalPayment);
        loan.setTotalAmountPaid(newTotalPaid);

        BigDecimal newTotalLateFee = loan.getTotalLateFee().add(lateFee);
        loan.setTotalLateFee(newTotalLateFee);

        BigDecimal newTotalInterestPaid = loan.getTotalInterestPaid().add(emi.getInterestComponent());
        loan.setTotalInterestPaid(newTotalInterestPaid);

        // remaining amount = previous outstanding - EMI amount
        BigDecimal newOutstanding = loan.getOutstandingAmount().subtract(emi.getPrincipalComponent());
        if (newOutstanding.compareTo(BigDecimal.ZERO) < 0) {
            newOutstanding = BigDecimal.ZERO;
        }
        loan.setOutstandingAmount(newOutstanding);

        // update EMI count
        loan.setTotalPaidEmiCount(loan.getTotalPaidEmiCount() + 1);

        // update next due date
        loanEmiScheduleRepository.findByLoan_LoanId(loanId).stream()
                .filter(e -> e.getStatus() == EmiStatus.UNPAID)
                .min(Comparator.comparing(LoanEmiSchedule::getDueDate))
                .ifPresentOrElse(
                        next -> loan.setNextDueDate(next.getDueDate()),
                        () -> loan.setNextDueDate(null)
                );

        // if all EMIs are done → close loan
        boolean allPaid = loanEmiScheduleRepository.findByLoan_LoanId(loanId).stream()
                .allMatch(e -> e.getStatus() == EmiStatus.PAID || e.getStatus() == EmiStatus.LATE);

        if (allPaid) {
            loan.setStatus(LoanStatus.CLOSED);
            loan.setOutstandingAmount(BigDecimal.ZERO);
            loan.setNextDueDate(null);
        }

        loansRepository.save(loan);
    }

    @Override
    public LoanDetailsResponse getLoanDetailsById(Long loanId) {

        Loans loan = loansRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Loan not found with ID: " + loanId));

        HomeLoanDetails homeDetails = homeLoanRepo.findByLoans_LoanId(loanId).orElse(null);
        CarLoanDetails carDetails = carLoanRepo.findByLoans_LoanId(loanId).orElse(null);
        List<LoanEmiSchedule> emiList = loanEmiScheduleRepository.findByLoan_LoanId(loanId);

        return mapper.toLoanDetailsResponse(loan, homeDetails, carDetails);
    }

    @Override
    public List<LoanDetailsResponse> getLoansByCif(String cifNumber) {
        return loansRepository.findByCifNumber(cifNumber).stream()
                .map(loan -> getLoanDetailsById(loan.getLoanId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<EmiSummary> getAllEmisByLoanId(Long loanId) {
        return loanEmiScheduleRepository.findByLoan_LoanId(loanId).stream()
                .map(mapper::toEmiSummary)
                .collect(Collectors.toList());
    }

    @Override
    public EmiSummary getEmiById(Long loanId, Long emiId) {
        LoanEmiSchedule emi = loanEmiScheduleRepository.findByIdAndLoan_LoanId(emiId, loanId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "EMI not found for loanId: " + loanId + " and emiId: " + emiId));
        return mapper.toEmiSummary(emi);
    }

    @Override
    @Transactional
    public LoanPrepaymentResponse makePrepayment(Long loanId, LoanPrepaymentRequest request) {

        Loans loan = loansRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        BigDecimal prepaymentAmount = request.getPrepaymentAmount();

        if (prepaymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid prepayment amount");
        }

        // Reduce principal
        BigDecimal newPrincipal = loan.getOutstandingAmount().subtract(prepaymentAmount);
        if (newPrincipal.compareTo(BigDecimal.ZERO) < 0) {
            newPrincipal = BigDecimal.ZERO;
        }

        // Update loan outstanding amount
        loan.setOutstandingAmount(newPrincipal);

        int monthsSinceDisbursement = Period.between(loan.getDisbursementDate(), LocalDate.now()).getMonths();
        BigDecimal foreclosureCharge = BigDecimal.ZERO;

        switch (loan.getLoanType()) {
            case CAR -> {
                if (monthsSinceDisbursement < 6) {
                    throw new IllegalStateException("Full prepayment allowed only after 6 months for Car Loan.");
                }
            }
            case HOME -> {
                if (monthsSinceDisbursement < 6) {
                    throw new IllegalStateException("Full prepayment allowed only after 6 months for Home Loan.");
                }

            }
        }

        if (loan.getDisbursementDate().plusMonths(6).isAfter(LocalDate.now())) {
            throw new IllegalStateException("Full repayment not allowed within first 6 months of loan disbursement.");
        }

        // Handle FULL PREPAYMENT (Loan Closure)
        if (newPrincipal.compareTo(BigDecimal.ZERO) == 0) {
            loan.setStatus(LoanStatus.CLOSED);
            loanEmiScheduleRepository.deleteAll(
                    loanEmiScheduleRepository.findByLoanAndStatus(loan, EmiStatus.UNPAID)
            );

            LoanPrepayment record = LoanPrepayment.builder()
                    .loans(loan)
                    .prepaymentAmount(prepaymentAmount)
                    .prepaymentOption(request.getOption())
                    .prepaymentDate(LocalDate.now())
                    .newOutstandingPrincipal(BigDecimal.ZERO)
                    .newEmi(BigDecimal.ZERO)
                    .newTenureMonths(0)
                    .remarks("Full prepayment done. Loan closed successfully.")
                    .build();

            loanPrepaymentRepo.save(record);
            loansRepository.save(loan);

            return LoanPrepaymentResponse.builder()
                    .loanId(loanId)
                    .newPrincipal(BigDecimal.ZERO)
                    .newEmi(BigDecimal.ZERO)
                    .newTenureMonths(0)
                    .message("Loan fully repaid and closed. No further EMIs required.")
                    .build();
        }


        // Partial prepayment — adjust EMI or tenure
        List<LoanEmiSchedule> remainingEmis =
                loanEmiScheduleRepository.findByLoanAndStatus(loan, EmiStatus.UNPAID);

        int remainingMonths = remainingEmis.size();
        BigDecimal annualRate = loan.getInterestRate();

        BigDecimal newEmi;
        int newTenureMonths = remainingMonths;

        if (request.getOption() == PrepaymentOption.REDUCE_TENURE) {
            // keep EMI same, reduce tenure
            newEmi = remainingEmis.get(0).getEmiAmount();
            newTenureMonths = calculateNewTenure(newPrincipal, annualRate, newEmi);
        } else {
            // keep tenure same, reduce EMI
            newEmi = calculateEmi(newPrincipal, annualRate, remainingMonths);
        }

        // Record prepayment
        LoanPrepayment record = LoanPrepayment.builder()
                .loans(loan)
                .prepaymentAmount(prepaymentAmount)
                .prepaymentOption(request.getOption())
                .prepaymentDate(LocalDate.now())
                .newOutstandingPrincipal(newPrincipal)
                .newEmi(newEmi)
                .newTenureMonths(newTenureMonths)
                .remarks("Prepayment processed successfully")
                .build();

        loanPrepaymentRepo.save(record);


        // Delete old unpaid EMIs
        loanEmiScheduleRepository.deleteAll(remainingEmis);

        // Generate new EMI schedule
        generateNewEmiSchedule(loan, newPrincipal, annualRate, newTenureMonths, newEmi);

        loansRepository.save(loan);

        return LoanPrepaymentResponse.builder()
                .loanId(loanId)
                .newPrincipal(newPrincipal)
                .newEmi(newEmi)
                .newTenureMonths(newTenureMonths)
                .message("Prepayment processed successfully with option: " + request.getOption())
                .build();
    }


    private BigDecimal calculateEmi(BigDecimal principal, BigDecimal annualRate, int months) {
        if (principal == null || principal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Principal must be greater than zero");
        }
        if (annualRate == null || annualRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Interest rate must be greater than zero");
        }
        if (months <= 0) {
            throw new IllegalArgumentException("Tenure months must be greater than zero");
        }

        // Convert annual interest rate to monthly decimal rate
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12 * 100), 10, RoundingMode.HALF_UP);

        // If monthly rate is zero (edge case), just return principal / months
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
        }

        // EMI formula: [P × r × (1 + r)^n] / [(1 + r)^n – 1]
        BigDecimal onePlusRPowerN = BigDecimal.ONE.add(monthlyRate).pow(months);
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusRPowerN);
        BigDecimal denominator = onePlusRPowerN.subtract(BigDecimal.ONE);

        if (denominator.compareTo(BigDecimal.ZERO) == 0) {
            throw new ArithmeticException("Invalid EMI calculation: denominator became zero");
        }

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateLateFee(BigDecimal emiAmount, long daysLate) {
        BigDecimal dailyRate = new BigDecimal("0.001"); // 0.1% per day on EMI
        return emiAmount.multiply(dailyRate)
                .multiply(BigDecimal.valueOf(daysLate))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private LoanEmiScheduleResponse mapToResponse(LoanEmiSchedule emi) {
        return LoanEmiScheduleResponse.builder()
                .id(emi.getId())
                .installmentNumber(emi.getInstallmentNumber())
                .dueDate(emi.getDueDate())
                .emiAmount(emi.getEmiAmount())
                .principalComponent(emi.getPrincipalComponent())
                .interestComponent(emi.getInterestComponent())
                .status(emi.getStatus())
                .paymentDate(emi.getPaymentDate())
                .lateFee(emi.getLateFee())
                .build();
    }

    private int calculateNewTenure(BigDecimal principal, BigDecimal annualRate, BigDecimal emi) {
        double P = principal.doubleValue();
        double R = annualRate.doubleValue() / 12 / 100;
        double E = emi.doubleValue();

        // formula: n = log(E / (E - P*R)) / log(1 + R)
        double n = Math.log(E / (E - P * R)) / Math.log(1 + R);
        return (int) Math.ceil(n);
    }


    private void generateNewEmiSchedule(Loans loan, BigDecimal principal, BigDecimal annualRate, int months, BigDecimal emi) {
        double remainingPrincipal = principal.doubleValue();
        double R = annualRate.doubleValue() / 12 / 100;
        LocalDate nextDue = LocalDate.now().plusMonths(1);

        for (int i = 1; i <= months; i++) {
            double interest = remainingPrincipal * R;
            double principalComponent = emi.doubleValue() - interest;
            remainingPrincipal -= principalComponent;
            if (remainingPrincipal < 0) remainingPrincipal = 0;

            LoanEmiSchedule schedule = LoanEmiSchedule.builder()
                    .loan(loan)
                    .installmentNumber(i)
                    .dueDate(nextDue)
                    .emiAmount(emi)
                    .interestComponent(BigDecimal.valueOf(interest).setScale(2, RoundingMode.HALF_UP))
                    .principalComponent(BigDecimal.valueOf(principalComponent).setScale(2, RoundingMode.HALF_UP))
                    .status(EmiStatus.UNPAID)
                    .build();

            loanEmiScheduleRepository.save(schedule);
            nextDue = nextDue.plusMonths(1);
        }
    }
}
