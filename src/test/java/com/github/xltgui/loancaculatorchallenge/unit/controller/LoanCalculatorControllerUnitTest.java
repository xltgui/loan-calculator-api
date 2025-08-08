package com.github.xltgui.loancaculatorchallenge.unit.controller;

import com.github.xltgui.loancaculatorchallenge.api.LoanRequest;
import com.github.xltgui.loancaculatorchallenge.api.PaymentDetailResponse;
import com.github.xltgui.loancaculatorchallenge.model.Loan;
import com.github.xltgui.loancaculatorchallenge.model.LoanCalculationService;
import com.github.xltgui.loancaculatorchallenge.model.PaymentDetail;
import com.github.xltgui.loancaculatorchallenge.web.LoanCalculatorController;
import com.github.xltgui.loancaculatorchallenge.web.LoanCalculatorMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Unit tests for loan calculator controller")
@ExtendWith(MockitoExtension.class)
public class LoanCalculatorControllerUnitTest {
    @Mock
    private LoanCalculationService service;

    @Mock
    private LoanCalculatorMapper mapper;

    @InjectMocks
    private LoanCalculatorController controller;

    private LoanRequest testLoanRequest;
    private PaymentDetail testPaymentDetailEntity;
    private PaymentDetailResponse testPaymentDetailResponse;

    @BeforeEach
    void setUp() {
        testLoanRequest = new LoanRequest(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31),
                LocalDate.of(2025, 2, 1),
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(5)
        );

        Loan loan = new Loan();
        loan.setId(1L);
        loan.setStartDate(testLoanRequest.startDate());
        loan.setEndDate(testLoanRequest.endDate());
        loan.setFirstPaymentDate(testLoanRequest.firstPaymentDate());
        loan.setAmount(testLoanRequest.amount());
        loan.setInterestRate(testLoanRequest.interestRate());

        testPaymentDetailEntity = new PaymentDetail();
        testPaymentDetailEntity.setId(1L);
        testPaymentDetailEntity.setCompetenceDate(LocalDate.of(2025, 2, 1));
        testPaymentDetailEntity.setLoanAmount(BigDecimal.valueOf(10000));
        testPaymentDetailEntity.setOutstandingBalance(BigDecimal.valueOf(9185.60));
        testPaymentDetailEntity.setConsolidated("Consolidate test");
        testPaymentDetailEntity.setInstallmentAmount(BigDecimal.valueOf(856.07));
        testPaymentDetailEntity.setPrincipalAmortization(BigDecimal.valueOf(814.40));
        testPaymentDetailEntity.setPrincipalBalance(BigDecimal.valueOf(9185.60));
        testPaymentDetailEntity.setProvision(BigDecimal.valueOf(0.00));
        testPaymentDetailEntity.setAccumulatedInterest(BigDecimal.valueOf(0.00));
        testPaymentDetailEntity.setPaidAmount(BigDecimal.valueOf(0.00));
        testPaymentDetailEntity.setLoan(loan);

        testPaymentDetailResponse = new PaymentDetailResponse(
                "Consolidate test",
                LocalDate.of(2025, 2, 1),
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(9185.60),
                BigDecimal.valueOf(856.07),
                BigDecimal.valueOf(814.40),
                BigDecimal.valueOf(9185.60),
                BigDecimal.valueOf(0.00),
                BigDecimal.valueOf(0.00),
                BigDecimal.valueOf(0.00)
        );
    }

    @Test
    @DisplayName("Must calculate and return payment details succesfully")
    void shouldCalculateAndReturnPaymentDetailsSuccessfully() {
        when(mapper.toEntity(testLoanRequest)).thenReturn(testPaymentDetailEntity.getLoan());

        when(service.caculateLoanDetails(testPaymentDetailEntity.getLoan()))
                .thenReturn(Collections.singletonList(testPaymentDetailEntity));

        when(mapper.toDtoList(Collections.singletonList(testPaymentDetailEntity)))
                .thenReturn(Collections.singletonList(testPaymentDetailResponse));

        ResponseEntity<List<PaymentDetailResponse>> responseEntity = controller.calculate(testLoanRequest);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody()).hasSize(1);

        PaymentDetailResponse response = responseEntity.getBody().get(0);
        assertThat(response.consolidated()).isEqualTo("Consolidate test");
        assertThat(response.competenceDate()).isEqualTo(LocalDate.of(2025, 2, 1));

        assertThat(response.loanAmount()).isEqualByComparingTo("10000");
        assertThat(response.outstandingBalance()).isEqualByComparingTo("9185.60");
        assertThat(response.installmentAmount()).isEqualByComparingTo("856.07");
        assertThat(response.principalAmortization()).isEqualByComparingTo("814.40");

        verify(mapper).toEntity(testLoanRequest);
        verify(service).caculateLoanDetails(testPaymentDetailEntity.getLoan());
        verify(mapper).toDtoList(Collections.singletonList(testPaymentDetailEntity));
    }

    @Test
    @DisplayName("Must return an empty list if service doesnt return details")
    void shouldReturnEmptyListIfServiceReturnsNoDetails() {
        when(mapper.toEntity(testLoanRequest)).thenReturn(testPaymentDetailEntity.getLoan());

        when(service.caculateLoanDetails(testPaymentDetailEntity.getLoan()))
                .thenReturn(Collections.emptyList());

        when(mapper.toDtoList(Collections.emptyList()))
                .thenReturn(Collections.emptyList());

        ResponseEntity<List<PaymentDetailResponse>> responseEntity = controller.calculate(testLoanRequest);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody()).isEmpty();

        verify(mapper).toEntity(testLoanRequest);
        verify(service).caculateLoanDetails(testPaymentDetailEntity.getLoan());
        verify(mapper).toDtoList(Collections.emptyList());
    }
}
