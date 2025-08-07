package com.github.xltgui.loancaculatorchallenge.api;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentDetailResponse(
         String consolidated,
         LocalDate competenceDate,
         BigDecimal loanAmount,
         BigDecimal outstandingBalance,
         BigDecimal installmentAmount,
         BigDecimal principalAmortization,
         BigDecimal principalBalance,
         BigDecimal provision,
         BigDecimal accumulatedInterest,
         BigDecimal paidAmount
){
}
