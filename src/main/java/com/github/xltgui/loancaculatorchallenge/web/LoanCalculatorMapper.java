package com.github.xltgui.loancaculatorchallenge.web;

import com.github.xltgui.loancaculatorchallenge.api.LoanRequest;
import com.github.xltgui.loancaculatorchallenge.api.PaymentDetailResponse;
import com.github.xltgui.loancaculatorchallenge.model.Loan;
import com.github.xltgui.loancaculatorchallenge.model.PaymentDetail;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LoanCalculatorMapper {
    public Loan toEntity(LoanRequest dto) {
        return Loan.builder()
                .startDate(dto.startDate())
                .endDate(dto.endDate())
                .amount(dto.amount())
                .firstPaymentDate(dto.firstPaymentDate())
                .interestRate(dto.interestRate())
                .baseDays(360) // hardcoded como solicitado
                .totalPayments(120) // hardcoded como solicitado
                .build();
    }

    public List<PaymentDetailResponse> toDtoList(List<PaymentDetail> paymentDetails) {
        List<PaymentDetailResponse> details = new ArrayList<>();
        for(PaymentDetail detail : paymentDetails){
            details.add(new PaymentDetailResponse(
                    detail.getConsolidated(),
                    detail.getCompetenceDate(),
                    detail.getLoanAmount(),
                    detail.getPrincipalBalance(),
                    detail.getOutstandingBalance(),
                    detail.getInstallmentAmount(),
                    detail.getPrincipalAmortization(),
                    detail.getAccumulatedInterest(),
                    detail.getPaidAmount(),
                    detail.getProvision()
            ));
        }
        return details;
    }
}
