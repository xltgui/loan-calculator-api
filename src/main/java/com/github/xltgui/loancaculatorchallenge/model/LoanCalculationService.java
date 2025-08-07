package com.github.xltgui.loancaculatorchallenge.model;

import com.github.xltgui.loancaculatorchallenge.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

@Service
@RequiredArgsConstructor
public class LoanCalculationService {
    private final LoanRepository loanRepository;
    private final LoanCalculatorValidator validator;

    @Transactional
    public List<PaymentDetail> caculateLoanDetails(Loan loan) {
        validator.valid(loan);

        List<PaymentDetail> paymentDetails = new ArrayList<>();

        int consolidatedCount = 0;
        boolean isPaymentDay;
        boolean isEndDate;

        boolean isPaymentInTheLastDayOfMonth = loan.getFirstPaymentDate().lengthOfMonth() == loan.getFirstPaymentDate().getDayOfMonth();

        BigDecimal currentOutstandingBalance = loan.getAmount();
        BigDecimal currentProvision = BigDecimal.ZERO;
        LocalDate previousCompetenceDate = loan.getStartDate();

        TreeSet<LocalDate> competenceDates = determineAllCompetenceDates(loan, isPaymentInTheLastDayOfMonth);

        if(competenceDates.first().equals(loan.getStartDate())) {
            PaymentDetail initialDetail = setInitialPayment(loan);

            paymentDetails.add(initialDetail);

            previousCompetenceDate = loan.getStartDate();
            currentOutstandingBalance = loan.getAmount();
            currentProvision = BigDecimal.ZERO;
        }

        competenceDates.removeFirst();

        for(LocalDate currentCompetenceDate : competenceDates) {
            // Diferença entre a data atual e a data anterior
            int daysInPeriod = Math.toIntExact(ChronoUnit.DAYS.between(previousCompetenceDate, currentCompetenceDate));
            if (daysInPeriod == 0) continue;

            BigDecimal daysDec = BigDecimal.valueOf(daysInPeriod);
            BigDecimal baseDaysDec = BigDecimal.valueOf(loan.getBaseDays());

            // Provisão
            BigDecimal annualRate = loan.getInterestRate().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
            BigDecimal exponent = daysDec.divide(baseDaysDec, 10, RoundingMode.HALF_UP);
            BigDecimal ratePlusOne = annualRate.add(BigDecimal.ONE);
            BigDecimal power = BigDecimal.valueOf(Math.pow(ratePlusOne.doubleValue(), exponent.doubleValue()));
            BigDecimal provision = power.subtract(BigDecimal.ONE).multiply(currentOutstandingBalance.add(currentProvision)).setScale(2, RoundingMode.HALF_UP);

            PaymentDetail detail = new PaymentDetail();
            detail.setCompetenceDate(currentCompetenceDate);
            detail.setLoanAmount(BigDecimal.ZERO);
            detail.setLoan(loan);

            isEndDate = competenceDates.last().isEqual(currentCompetenceDate);

            isPaymentDay = validatePaymentDay(currentCompetenceDate, isPaymentInTheLastDayOfMonth, isEndDate, loan);

            if(isPaymentDay) {
                consolidatedCount++;
                detail.setConsolidated(consolidatedCount + "/" + loan.getTotalPayments());

                // Parcela
                BigDecimal amortization = loan.getAmount().divide(BigDecimal.valueOf(loan.getTotalPayments()), 2, RoundingMode.HALF_UP);

                if(isEndDate) amortization = currentOutstandingBalance;

                BigDecimal installmentAmount = amortization.add(currentProvision).add(provision).setScale(2, RoundingMode.HALF_UP);
                detail.setInstallmentAmount(installmentAmount);

                // Principal
                detail.setPrincipalAmortization(amortization);

                currentOutstandingBalance = currentOutstandingBalance.subtract(amortization).setScale(2, RoundingMode.HALF_UP);
                if(isEndDate) {
                    currentOutstandingBalance = BigDecimal.ZERO;
                }

                detail.setPrincipalBalance(currentOutstandingBalance);

                // Juros
                detail.setProvision(provision);
                detail.setAccumulatedInterest(BigDecimal.ZERO);
                detail.setPaidAmount(currentProvision.add(provision).setScale(2, RoundingMode.HALF_UP));

                // Saldo devedor
                detail.setOutstandingBalance(detail.getPrincipalBalance().add(detail.getAccumulatedInterest()));

                currentProvision = BigDecimal.ZERO;
            } else {
                detail.setConsolidated("");

                detail.setInstallmentAmount(BigDecimal.ZERO);
                detail.setPrincipalAmortization(BigDecimal.ZERO);
                detail.setPrincipalBalance(currentOutstandingBalance);

                detail.setProvision(provision);
                detail.setAccumulatedInterest(currentProvision.add(provision).setScale(2, RoundingMode.HALF_UP));
                detail.setPaidAmount(BigDecimal.ZERO);

                // Saldo devedor
                detail.setOutstandingBalance(detail.getPrincipalBalance().add(detail.getAccumulatedInterest()));

                // Atualiza a provisão acumulada
                currentProvision = currentProvision.add(provision).setScale(2, RoundingMode.HALF_UP);
            }
            paymentDetails.add(detail);
            previousCompetenceDate = currentCompetenceDate;
        }

        loan.setPaymentDetails(paymentDetails);
        // Obs: nao é necessario salvar paymentDetails em seu proprio repositorio por conta do CascadeType.ALL
        loanRepository.save(loan);

        return paymentDetails;
    }

    private boolean validatePaymentDay(LocalDate competenceDate, boolean isPaymentInTheLastDayOfMonth, boolean isEndDate, Loan loan) {
        if(isEndDate) return true;
        if(competenceDate.getDayOfMonth() == loan.getFirstPaymentDate().getDayOfMonth()) return true;

        // Garantir que o ultimo dia de pagamento de cada mês seja adicionado caso o primeiro pagamento tenha sido feito no final do mês
        if(isPaymentInTheLastDayOfMonth){
            return competenceDate.lengthOfMonth() == competenceDate.getDayOfMonth();
        }
        return false;
    }

    private static PaymentDetail setInitialPayment(Loan loan) {
        PaymentDetail initialDetail = new PaymentDetail();

        // Empréstimo
        initialDetail.setCompetenceDate(loan.getStartDate());
        initialDetail.setLoanAmount(loan.getAmount());
        initialDetail.setOutstandingBalance(loan.getAmount());

        // Parcela
        initialDetail.setConsolidated("");
        initialDetail.setInstallmentAmount(BigDecimal.ZERO);

        // Principal
        initialDetail.setPrincipalAmortization(BigDecimal.ZERO);
        initialDetail.setPrincipalBalance(loan.getAmount());

        // Juros
        initialDetail.setProvision(BigDecimal.ZERO);
        initialDetail.setAccumulatedInterest(BigDecimal.ZERO);
        initialDetail.setPaidAmount(BigDecimal.ZERO);

        initialDetail.setLoan(loan);

        return initialDetail;
    }

    private TreeSet<LocalDate> determineAllCompetenceDates(Loan loan, boolean isPaymentInTheLastDayOfMonth) {
        TreeSet<LocalDate> competenceDates = new TreeSet<>();

        competenceDates.add(loan.getStartDate());
        competenceDates.add(loan.getFirstPaymentDate());

        // Caso de uso: Data final sempre será um pagamento de parcela
        competenceDates.add(loan.getEndDate());

        LocalDate currentPaymentDate = loan.getFirstPaymentDate();

        while (!currentPaymentDate.isAfter(loan.getEndDate())) {
            competenceDates.add(currentPaymentDate);

            LocalDate lastDayOfPreviousMonth = currentPaymentDate.minusMonths(1)
                    .withDayOfMonth(currentPaymentDate.minusMonths(1).lengthOfMonth());

            // Evitar adicionar o mês anterior a data inicial caso a data de pagamento seja no mesmo mês da data inicial
            if (lastDayOfPreviousMonth.isAfter(loan.getStartDate()) && !lastDayOfPreviousMonth.isAfter(loan.getEndDate())) {
                // Adiciona a ultima data do mês anterior somente se o dia de pagamento nao é no ultimo dia do mês
                if(!isPaymentInTheLastDayOfMonth) {
                    competenceDates.add(lastDayOfPreviousMonth);
                }
            }

            // Adicionar a data do ultimo dia do mês anterior a data final
            if(Math.abs(ChronoUnit.MONTHS.between(YearMonth.from((loan.getEndDate())), YearMonth.from(currentPaymentDate))) == 1){
                LocalDate lastDayOfCurrentMonth = currentPaymentDate.withDayOfMonth(currentPaymentDate.lengthOfMonth());
                competenceDates.add(lastDayOfCurrentMonth);
            }

            if(isPaymentInTheLastDayOfMonth) {
                // Caso de uso: Se a data de primeiro pagamento for no ultimo dia do mês, o próximo pagamento deverá cair no final do próximo mês
                currentPaymentDate = currentPaymentDate.plusMonths(1)
                        .withDayOfMonth(currentPaymentDate.plusMonths(1).lengthOfMonth());
            }else{
                currentPaymentDate = currentPaymentDate.plusMonths(1);
            }
        }
        return competenceDates;
    }
}
