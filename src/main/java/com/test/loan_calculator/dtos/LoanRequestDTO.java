package com.test.loan_calculator.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoanRequestDTO {

    @NotNull
    private LocalDate startDate;
    
    @NotNull
    private LocalDate endDate;
    
    @NotNull
    private LocalDate firstPaymentDate;
    
    @NotNull
    private BigDecimal loanAmount;
    
    @NotNull
    private BigDecimal interestRate;
}
