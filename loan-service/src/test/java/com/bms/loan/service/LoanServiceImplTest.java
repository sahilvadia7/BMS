package com.bms.loan.service;

import com.bms.loan.Repository.CarLoanRepository;
import com.bms.loan.Repository.InterestRateRepository;
import com.bms.loan.Repository.LoanHistoryDetailsRepository;
import com.bms.loan.Repository.LoanRepository;
import com.bms.loan.Repository.education.EducationLoanRepository;
import com.bms.loan.Repository.home.HomeLoanRepository;
import com.bms.loan.dto.request.CustomerRegisterRequest;
import com.bms.loan.dto.request.car.CarLoanDetailsDto;
import com.bms.loan.dto.request.home.HomeLoanDetailsDto;
import com.bms.loan.dto.request.loan.LoanApplicationRequest;
import com.bms.loan.dto.request.loan.LoanHistory.ActiveLoanDto;
import com.bms.loan.dto.request.loan.LoanHistory.ClosedLoanDto;
import com.bms.loan.dto.request.loan.LoanHistory.LoanHistoryDetailsDto;
import com.bms.loan.dto.response.CustomerDetailsResponseDTO;
import com.bms.loan.dto.response.CustomerResponseDTO;
import com.bms.loan.dto.response.loan.LoanApplicationResponse;
import com.bms.loan.entity.InterestRate;
import com.bms.loan.entity.loan.LoanHistoryDetails;
import com.bms.loan.entity.loan.Loans;
import com.bms.loan.enums.EmploymentType;
import com.bms.loan.enums.Gender;
import com.bms.loan.enums.LoanStatus;
import com.bms.loan.enums.LoanType;
import com.bms.loan.feign.CustomerClient;
import com.bms.loan.feign.NotificationClient;
import com.bms.loan.service.impl.LoanApplicationServiceImpl;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
public class LoanServiceImplTest {

    @InjectMocks
    private LoanApplicationServiceImpl loanService;

    @Mock
    private InterestRateRepository interestRateRepository;
    @Mock
    private LoanRepository loanRepository;
    @Mock
    private LoanHistoryDetailsRepository loanHistoryDetailsRepository;
    @Mock
    private CarLoanRepository carLoanRepository;
    @Mock
    private HomeLoanRepository  homeLoanRepository;
    @Mock
    private EducationLoanRepository educationLoanRepository;
    @Mock
    private CustomerClient customerClient;
    @Mock
    private NotificationClient notificationClient;

    private LoanApplicationRequest request;

    @BeforeEach
    void setup() {
        request = new LoanApplicationRequest();
        request.setLoanType(LoanType.CAR);
        request.setRequestedAmount(BigDecimal.valueOf(500000));
        request.setRequestedTenureMonths(24);
        request.setMonthlyIncome(BigDecimal.valueOf(30000));
        request.setBankName("HDFC");
        request.setBankAccount("1111222233");
        request.setIfscCode("HDFC0001");
        request.setEmploymentType(EmploymentType.SALARIED);

        CustomerRegisterRequest customer = new CustomerRegisterRequest();
        customer.setFirstName("Penil");
        customer.setLastName("Hirapara");
        customer.setEmail("qtpenil@gmail.com");
        customer.setAddress("Ahemdabad");
        customer.setDob("25/10/2003");
        customer.setGender(String.valueOf(Gender.MALE));

        request.setCustomerDetails(customer);

        CarLoanDetailsDto car = new CarLoanDetailsDto();
        car.setCarModel("Verna");
        car.setManufacturer("Hundai");
        car.setManufactureYear(2024);
        car.setCarValue(BigDecimal.valueOf(600000));
        car.setRegistrationNumber("GJ01AB1234");
        car.setDownPayment(BigDecimal.valueOf(50000));

        request.setCarDetails(car);

        HomeLoanDetailsDto home = new HomeLoanDetailsDto();
        home.setDownPayment(BigDecimal.valueOf(500000));
        home.setBuilderName("Jenil Modi Group");
        home.setRegistrationNumber("ABCD1234");
        home.setPropertyType("PERSONAL");
        home.setPropertyValue(BigDecimal.valueOf(3500000));
        home.setPropertyAddress("Ahemdabad");

        request.setHomeDetails(home);

        LoanHistoryDetailsDto loanHistoryDto = new LoanHistoryDetailsDto();

        loanHistoryDto.setHaveExistingLoans(true);
        loanHistoryDto.setTotalOutstandingAmount(BigDecimal.valueOf(100000));
        loanHistoryDto.setTotalMonthlyEmi(BigDecimal.valueOf(5000));
        loanHistoryDto.setTotalClosedLoans(1);
        loanHistoryDto.setTotalActiveLoans(1);

        // Active loan
        ActiveLoanDto activeLoanDto = new ActiveLoanDto();
        activeLoanDto.setLoanType("PERSONAL");
        activeLoanDto.setLoanAmount(BigDecimal.valueOf(50000));
        activeLoanDto.setTenureMonths(12);
        activeLoanDto.setRemainingAmount(BigDecimal.valueOf(20000));
        activeLoanDto.setEmiAmount(BigDecimal.valueOf(5000));

        loanHistoryDto.setActiveLoans(List.of(activeLoanDto));

        // Closed loan
        ClosedLoanDto closedLoanDto = new ClosedLoanDto();
        closedLoanDto.setLoanType("CAR");
        closedLoanDto.setLoanAmount(BigDecimal.valueOf(100000));
        closedLoanDto.setStartDate(LocalDate.of(2022,1,1));
        closedLoanDto.setEndDate(LocalDate.of(2023,1,1));
        closedLoanDto.setClosedOnTime(true);

        loanHistoryDto.setClosedLoans(List.of(closedLoanDto));

        request.setLoanHistoryDetailsDto(loanHistoryDto);
    }

    @Test
    void testApplyLoan_CustomerExist_Success(){

        // Mock interest rate
        InterestRate rate = new InterestRate();
        rate.setBaseRate(BigDecimal.valueOf(0.01));
        rate.setMinTenure(12);
        rate.setMaxTenure(60);
        rate.setLoanType(String.valueOf(LoanType.CAR));

        when(interestRateRepository.findByLoanType(String.valueOf(LoanType.CAR))).thenReturn(Optional.of(rate));

        // Mock customer exists
        CustomerDetailsResponseDTO customer = new CustomerDetailsResponseDTO();
        customer.setCustomerId(1L);
        customer.setCifNumber("CIF1234");

        when(customerClient.getByCif(anyString())).thenReturn(customer);

        // Mock saving loan
        Loans savedLoan = Loans.builder()
                .loanId(10L)
                .cifNumber("CIF1234")
                .loanType(LoanType.CAR)
                .status(LoanStatus.APPLIED)
                .build();

        when(loanRepository.save(any(Loans.class))).thenReturn(savedLoan);

        LoanApplicationResponse savedLoanResponse = loanService.applyLoan(request);

        //verify
        assertEquals(10L,savedLoanResponse.getLoanId());
        assertEquals("CIF1234",savedLoanResponse.getCifNumber());
        verify(notificationClient ,times(1)).sendApplyLoanEmail(any());

    }

    @Test
    void testApplyLoan_CustomerNotFound_RegistersCustomer(){

        // Mock the interest rate
        InterestRate rate = new InterestRate();
        rate.setBaseRate(BigDecimal.valueOf(0.01));
        rate.setMinTenure(12);
        rate.setMaxTenure(60);
        rate.setLoanType(String.valueOf(LoanType.CAR));

        when(interestRateRepository.findByLoanType("CAR"))
                .thenReturn(Optional.of(rate));

        // Mock 404 from getByCif
        FeignException notFound = mock(FeignException.NotFound.class);
        when(notFound.status()).thenReturn(404);
        when(customerClient.getByCif(anyString())).thenThrow(notFound);


        // Mock customer registration
        CustomerResponseDTO registered = new CustomerResponseDTO();
        registered.setCId(2L);
        registered.setCifNumber("CIF1234");

        when(customerClient.registerCustomer(any())).thenReturn(registered);

        Loans savedLoan = Loans.builder()
                .loanId(20L)
                .cifNumber("NEW001")
                .loanType(LoanType.CAR)
                .status(LoanStatus.APPLIED)
                .build();

        when(loanRepository.save(any(Loans.class))).thenReturn(savedLoan);

        when(carLoanRepository.save(any())).thenReturn(null);
//        doNothing().when(notificationClient).sendApplyLoanEmail(any());

        // Execute service
        LoanApplicationResponse savedLoanResponse = loanService.applyLoan(request);

        // Verify outputs
        assertEquals("CIF1234",savedLoanResponse.getCifNumber());
        assertEquals(20L, savedLoanResponse.getLoanId());

        // Verify customer registration was called exactly once
        verify(customerClient ,times(1)).registerCustomer(any());

        ArgumentCaptor<LoanHistoryDetails> captor = ArgumentCaptor.forClass(LoanHistoryDetails.class);
        verify(loanHistoryDetailsRepository).save(captor.capture());

        LoanHistoryDetails history = captor.getValue();

        assertEquals(true, history.isHaveExistingLoans());
        assertEquals(1, history.getActiveLoans().size());
        assertEquals(1, history.getClosedLoans().size());
    }


    @Test
    void testApplyLoan_TenureGreaterThanMax_ThrowsException() {

        // Mock interest rate
        InterestRate rate = new InterestRate();
        rate.setMaxTenure(10);
        when(interestRateRepository.findByLoanType("CAR"))
                .thenReturn(Optional.of(rate));

        request.setRequestedTenureMonths(20);

        assertThrows(RuntimeException.class, () -> loanService.applyLoan(request));
    }

}
