package com.github.xltgui.loancaculatorchallenge.web;

import com.github.xltgui.loancaculatorchallenge.api.LoanRequest;
import com.github.xltgui.loancaculatorchallenge.model.LoanCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/loan-calculate")
public class LoanCalculatorController {
    private final LoanCalculationService service;
    private final LoanMapper mapper;

    @PostMapping
    public Object calculate(@RequestBody LoanRequest request) {

        service.caculateLoanDetails(mapper.toEntity(request));
        return null;
    }
}
