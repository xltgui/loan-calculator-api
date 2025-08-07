package com.github.xltgui.loancaculatorchallenge.model;

import com.github.xltgui.loancaculatorchallenge.exception.InvalidDateDateException;
import org.springframework.stereotype.Component;

@Component
public class LoanCalculatorValidator {

    public void valid(Loan loan){
        // Caso de uso: A data final deve ser maior que a data inicial
        if(loan.getStartDate().isAfter(loan.getEndDate())){
            throw new InvalidDateDateException("End date must be after start date");
        }

        // Caso de uso: A data de primeiro pagamento deve ser maior que a data inicial e menor que a data final
        if(! (loan.getFirstPaymentDate().isAfter(loan.getStartDate()) && loan.getFirstPaymentDate().isBefore(loan.getEndDate())) ){
            throw new InvalidDateDateException("The first payment date must be between start date and end date");
        }
    }
}
