package com.test.loan_calculator.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.test.loan_calculator.dtos.LoanRequestDTO;
import com.test.loan_calculator.dtos.LoanResponseDTO;
import com.test.loan_calculator.service.LoanCalculatorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/loan-calculator")
@RequiredArgsConstructor
@Tag(name = "Loan", description = "Operações relacionadas a empréstimos")
public class LoanCalculatorController {

    @Autowired
    private LoanCalculatorService loanCalculatorService;

    @PostMapping("/calculate")
    @Operation(summary = "Calcula parcelas de empréstimo", description = "Retorna lista de parcelas com juros e amortização")
    public ResponseEntity<List<LoanResponseDTO>> calculate(@RequestBody LoanRequestDTO loan) {
        return ResponseEntity.ok(loanCalculatorService.calculate(loan));
    }
}
