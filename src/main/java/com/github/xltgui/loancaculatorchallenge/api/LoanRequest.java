package com.github.xltgui.loancaculatorchallenge.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoanRequest(
        @NotNull(message = "Field required!")
        LocalDate startDate,

        @NotNull(message = "Field required!")
        LocalDate endDate,

        @NotNull(message = "Field required!")
        LocalDate firstPaymentDate,

        @Positive(message = "Field must be positive and not null")
        BigDecimal amount,

        @Positive(message = "Field must be positive and not null")
        BigDecimal interestRate
) {
}
