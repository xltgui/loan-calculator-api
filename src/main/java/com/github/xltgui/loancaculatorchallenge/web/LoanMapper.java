package com.github.xltgui.loancaculatorchallenge.web;

import com.github.xltgui.loancaculatorchallenge.api.LoanRequest;
import com.github.xltgui.loancaculatorchallenge.model.Loan;
import org.springframework.stereotype.Component;

@Component
public class LoanMapper {
    public Loan toEntity(LoanRequest dto) {
        return Loan.builder()
                .startDate(dto.startDate())
                .endDate(dto.endDate())
                .amount(dto.amount())
                .firstPaymentDate(dto.firstPaymentDate())
                .interestRate(dto.interestRate())
                .baseDays(dto.baseDays())
                .totalPayments(dto.totalPayments())
                .build();
    }
}
