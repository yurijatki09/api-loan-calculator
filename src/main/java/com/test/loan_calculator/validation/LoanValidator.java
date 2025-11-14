package com.test.loan_calculator.validation;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.test.loan_calculator.dtos.LoanRequestDTO;

@Component
public class LoanValidator {

     public void validate(LoanRequestDTO request) {

        // Data final deve ser maior que data inicial
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("Data Incial deve ser anterior a Data Final.");
        }

        // Primeiro pagamento > data inicial
        if (request.getFirstPaymentDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("Data do primeiro pagamento deve ser após a Data Inicial.");
        }

        // Primeiro pagamento < data final
        if (request.getFirstPaymentDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("Data do primeiro pagamento deve ser anterior a Data Final.");
        }

        // Valor deve ser positivo
        if (request.getLoanAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor do emprestimo deve ser maior que zero.");
        }

        // Taxa não pode ser negativa
        if (request.getInterestRate().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Taxa de juros não pode ser negativa.");
        }
    }
}
