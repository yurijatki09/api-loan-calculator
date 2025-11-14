package com.test.loan_calculator.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoanResponseDTO {

    private LocalDate competenceDate;
    
    private BigDecimal loanValue;
    
    private BigDecimal outstandingBalance;
    
    private String installmentConsolidated;
    
    private BigDecimal installmentTotal;
    
    private BigDecimal amortization;
    
    private BigDecimal principalBalance;
    
    private BigDecimal interestProvision;
    
    private BigDecimal interestAccumulated;
    
    private BigDecimal interestPaid;
}
