package com.github.xltgui.loancaculatorchallenge.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "loan_details")
@Data
public class PaymentDetail {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private LocalDate competenceDate;
        private BigDecimal loanAmount;
        private BigDecimal outstandingBalance;

        private String consolidated;
        private BigDecimal installmentAmount;

        private BigDecimal principalAmortization;
        private BigDecimal principalBalance;

        private BigDecimal provision;
        private BigDecimal accumulatedInterest;
        private BigDecimal paidAmount;

        @ManyToOne
        @JoinColumn(name = "loan_id")
        private Loan loan;
}
