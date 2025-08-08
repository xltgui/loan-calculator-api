package com.github.xltgui.loancaculatorchallenge.web;

import com.github.xltgui.loancaculatorchallenge.api.LoanRequest;
import com.github.xltgui.loancaculatorchallenge.api.PaymentDetailResponse;
import com.github.xltgui.loancaculatorchallenge.model.LoanCalculationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/loan-calculate")
@CrossOrigin(origins = "http://localhost:4200")
public class LoanCalculatorController {
    private final LoanCalculationService service;
    private final LoanCalculatorMapper mapper;

    @PostMapping
    public ResponseEntity<List<PaymentDetailResponse>> calculate(@Valid @RequestBody LoanRequest request) {
        var entity = mapper.toEntity(request);
        var response = mapper.toDtoList(service.caculateLoanDetails(entity));
        return ResponseEntity.ok().body(response);
    }
}
