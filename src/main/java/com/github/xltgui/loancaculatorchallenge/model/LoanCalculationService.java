package com.github.xltgui.loancaculatorchallenge.model;

import com.github.xltgui.loancaculatorchallenge.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;

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

        BigDecimal principalBalance = loan.getAmount();
        BigDecimal accumulatedInterest = BigDecimal.ZERO;
        LocalDate previousCompetenceDate = loan.getStartDate();

        TreeSet<LocalDate> competenceDates = determineAllCompetenceDates(loan, isPaymentInTheLastDayOfMonth);

        if(competenceDates.first().equals(loan.getStartDate())) {
            PaymentDetail initialDetail = setInitialPayment(loan);
            paymentDetails.add(initialDetail);
            previousCompetenceDate = loan.getStartDate();
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
            BigDecimal provision = power.subtract(BigDecimal.ONE)
                    .multiply(principalBalance.add(accumulatedInterest))
                    .setScale(10, RoundingMode.HALF_UP);

            PaymentDetail detail = new PaymentDetail();
            detail.setCompetenceDate(currentCompetenceDate);
            detail.setLoanAmount(BigDecimal.ZERO);
            detail.setLoan(loan);

            isEndDate = competenceDates.last().isEqual(currentCompetenceDate);

            isPaymentDay = validatePaymentDay(currentCompetenceDate, isPaymentInTheLastDayOfMonth, isEndDate, loan);

            if(isPaymentDay) {
                consolidatedCount++;
                detail.setConsolidated(consolidatedCount + "/" + loan.getTotalPayments());

                // Amortização
                BigDecimal amortization = BigDecimal.ZERO;
                if(consolidatedCount <= loan.getTotalPayments()) {
                     amortization = loan.getAmount()
                             .divide(BigDecimal.valueOf(loan.getTotalPayments()), 10, RoundingMode.HALF_UP);
                }
                if(isEndDate) amortization = principalBalance;
                detail.setPrincipalAmortization(amortization.setScale(2, RoundingMode.HALF_UP));

                BigDecimal paidInterest = accumulatedInterest.add(provision);
                detail.setPaidAmount(paidInterest.setScale(2, RoundingMode.HALF_UP));

                // Total Parcela
                BigDecimal installmentAmount = amortization.add(paidInterest);
                detail.setInstallmentAmount(installmentAmount.setScale(2, RoundingMode.HALF_UP));

                // Saldo Principal
                principalBalance = principalBalance.subtract(amortization);
                detail.setPrincipalBalance(principalBalance.setScale(2, RoundingMode.HALF_UP));

                // Provisão
                detail.setProvision(provision.setScale(2, RoundingMode.HALF_UP));

                // Acumulado
                accumulatedInterest = accumulatedInterest.add(provision).subtract(paidInterest);

                // Saldo devedor
            } else {
                detail.setConsolidated("");

                detail.setInstallmentAmount(BigDecimal.ZERO);
                detail.setPrincipalAmortization(BigDecimal.ZERO);
                detail.setPaidAmount(BigDecimal.ZERO);

                detail.setPrincipalBalance(principalBalance.setScale(2, RoundingMode.HALF_UP));
                detail.setProvision(provision.setScale(2, RoundingMode.HALF_UP));

                accumulatedInterest = accumulatedInterest.add(provision);

                // Saldo devedor
            }
            detail.setAccumulatedInterest(accumulatedInterest.setScale(2, RoundingMode.HALF_UP));
            detail.setOutstandingBalance(principalBalance.add(accumulatedInterest).setScale(2, RoundingMode.HALF_UP));
            paymentDetails.add(detail);
            previousCompetenceDate = currentCompetenceDate;
        }

        loan.setPaymentDetails(paymentDetails);
        // Obs.: nao é necessario salvar paymentDetails em seu proprio repositorio por conta do CascadeType.ALL
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

        // Retorna verdadeiro se for dia não util e nao for o ultimo dia do mês
        // Com intuito de considerar a data convertida para um dia util como uma data de pagamento
        if(competenceDate.getDayOfMonth() != competenceDate.lengthOfMonth()){
            return isAnUtilDay(competenceDate, getHolidays(competenceDate.getYear()));
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


        List<LocalDate> utilDatesOnly = processDates(competenceDates, loan, true);
        competenceDates.addAll(utilDatesOnly); // adiciona as datas úteis convertidas

        List<LocalDate> datesToRemove = processDates(competenceDates, loan, false);
        datesToRemove.forEach(competenceDates::remove); // remove datas não úteis

        return competenceDates;
    }

    private List<LocalDate> processDates(TreeSet<LocalDate> currentDates, Loan loan, boolean returnNextUtilDay){
        List<LocalDate> dates = new ArrayList<>();

        for (LocalDate competenceDate : currentDates) {
            // Valida se nao é a data final
            if(competenceDate == loan.getEndDate() || competenceDate == loan.getStartDate()) continue;

            // Valida se nao é o último dia do mês
            if(competenceDate.getDayOfMonth() == competenceDate.lengthOfMonth()) continue;

            // Valida se nao é dia util
            if (!isAnUtilDay(competenceDate, getHolidays(competenceDate.getYear()))) {
                if(returnNextUtilDay){
                    dates.add(getNextUtilDay(competenceDate)); // retorna as datas convertidas
                }else{
                    dates.add(competenceDate); // retorna as datas para remover
                }
            }
        }
        return dates;
    }

    // Verifica se é um dia útil
    private boolean isAnUtilDay (LocalDate date, Set<LocalDate> holidays) {
        return !(date.getDayOfWeek() == DayOfWeek.SATURDAY ||
                date.getDayOfWeek() == DayOfWeek.SUNDAY ||
                holidays.contains(date));
    }

    private LocalDate getNextUtilDay(LocalDate date){
        LocalDate nextUtilDay = date.plusDays(1);

        // Retorna o próximo dia útil
        while(!isAnUtilDay(nextUtilDay, getHolidays(date.getYear()))){
            nextUtilDay = nextUtilDay.plusDays(1);
        }
        return nextUtilDay;
    }

    private Set<LocalDate> getHolidays (int year){
        Set<LocalDate> holidays = new HashSet<>();

        holidays.add(LocalDate.of(year, 1, 1));   // Confraternização Universal
        holidays.add(LocalDate.of(year, 4, 21));  // Tiradentes
        holidays.add(LocalDate.of(year, 5, 1));   // Dia do Trabalho
        holidays.add(LocalDate.of(year, 9, 7));   // Independência do Brasil
        holidays.add(LocalDate.of(year, 10, 12)); // Nossa Senhora Aparecida
        holidays.add(LocalDate.of(year, 11, 2));  // Finados
        holidays.add(LocalDate.of(year, 11, 15)); // Proclamação da República
        holidays.add(LocalDate.of(year, 12, 25)); // Natal

        return holidays;
    }
}
