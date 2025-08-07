package com.github.xltgui.loancaculatorchallenge.api;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoanRequest(
        LocalDate startDate,
        LocalDate endDate,
        LocalDate firstPaymentDate,
        BigDecimal amount,
        BigDecimal interestRate,
        Long baseDays,
        Long totalPayments
) {
}
