package com.github.xltgui.loancaculatorchallenge.repository;

import com.github.xltgui.loancaculatorchallenge.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, Long> {
}
