package com.github.xltgui.loancaculatorchallenge.model;

import com.github.xltgui.loancaculatorchallenge.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.event.spi.SaveOrUpdateEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

@Service
@RequiredArgsConstructor
public class LoanCalculationService {
    private final LoanRepository loanRepository;

    @Transactional
    public List<PaymentDetail> caculateLoanDetails(Loan loan) {
        List<PaymentDetail> paymentDetails = new ArrayList<>();

        Boolean isPaymentDay = false;
        BigDecimal currentOutstandingBalance = loan.getAmount();
        BigDecimal currentProvision = BigDecimal.ZERO;
        LocalDate previousCompetenceDate = loan.getStartDate();

        TreeSet<LocalDate> competenceDates = new TreeSet<>();
        competenceDates.add(loan.getStartDate());
        competenceDates.add(loan.getFirstPaymentDate());
        competenceDates.add(loan.getEndDate());

        LocalDate paymentDate = loan.getFirstPaymentDate();
        while (!paymentDate.isAfter(loan.getEndDate())) {
            competenceDates.add(paymentDate);

            System.out.println("Current date= " + paymentDate);
            LocalDate lastDayOfPreviousMonth = paymentDate.minusDays(1).withDayOfMonth(paymentDate.minusDays(1).lengthOfMonth());
            System.out.println("LAST DAY OF PREVIOUS MONTH=" + lastDayOfPreviousMonth);
            if (lastDayOfPreviousMonth.isAfter(previousCompetenceDate)) {
                competenceDates.add(lastDayOfPreviousMonth);
            }

            paymentDate = paymentDate.plusMonths(1);
        }
        System.out.println("FIRST=" + competenceDates.getFirst());

        if(competenceDates.first().equals(loan.getStartDate())) {
            PaymentDetail initialDetail = new PaymentDetail();

            // EMPRÉSTIMO
            initialDetail.setCompetenceDate(loan.getStartDate());
            initialDetail.setLoanAmount(loan.getAmount());
            initialDetail.setOutstandingBalance(loan.getAmount());

            // PARCELA
            initialDetail.setConsolidated("");
            initialDetail.setInstallmentAmount(BigDecimal.ZERO);

            // PRINCIPAL
            initialDetail.setPrincipalAmortization(BigDecimal.ZERO);
            initialDetail.setPrincipalBalance(loan.getAmount());

            // JUROS
            initialDetail.setProvision(BigDecimal.ZERO);
            initialDetail.setAccumulatedInterest(BigDecimal.ZERO);
            initialDetail.setPaidAmount(BigDecimal.ZERO);

            initialDetail.setLoan(loan);

            paymentDetails.add(initialDetail);
            previousCompetenceDate = loan.getStartDate();
            currentOutstandingBalance = loan.getAmount();
            currentProvision = BigDecimal.ZERO;
        }
        //System.out.println("PAYMENT DETAILS= " + paymentDetails);

        for(LocalDate currentCompetenceDate : competenceDates) {
            if(currentCompetenceDate.isBefore(previousCompetenceDate)) {
                System.out.println("CONTINUING=" + currentCompetenceDate + "|" + previousCompetenceDate);
                continue;
            }

            if(currentCompetenceDate.equals(loan.getStartDate())) {
                continue;
            }



            isPaymentDay = currentCompetenceDate.getDayOfMonth() == loan.getFirstPaymentDate().getDayOfMonth();

            if(isPaymentDay) {

            } else{

            }
        }


        /*paymentDetails.setLoan(loan);
        paymentDetails.setCompetenceDate(loan.getStartDate());
        paymentDetails.setOutStandingBalance(loan.getAmount());

        paymentDetails.setProvision(calculateProvision(loan));
        //paymentDetail.setAccumulatedInterest(calculateAccumulatedInterest());
        paymentDetails.setLoanAmount(loan.getAmount());*/
        return null;
    }

    private BigDecimal calculateProvision(Loan loan) {
        loan.getInterestRate();
        return null;
    }
}
