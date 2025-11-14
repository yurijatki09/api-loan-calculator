package com.test.loan_calculator.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.test.loan_calculator.dtos.LoanRequestDTO;
import com.test.loan_calculator.dtos.LoanResponseDTO;
import com.test.loan_calculator.validation.LoanValidator;

import lombok.val;

@Service
public class LoanCalculatorService {

    @Autowired
    private LoanValidator validator;

    public List<LoanResponseDTO> calculate(LoanRequestDTO loanRequest) {
        
        validator.validate(loanRequest);

        return new ArrayList<>();
    }
}
