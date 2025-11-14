package com.test.loan_calculator.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.test.loan_calculator.dtos.LoanRequestDTO;
import com.test.loan_calculator.dtos.LoanResponseDTO;
import com.test.loan_calculator.validation.LoanValidator;

@SpringBootTest
class LoanCalculatorServiceTest {

    @Mock
    private LoanValidator validator;

    @Autowired
    private LoanCalculatorService service;

    @Test
    void testCalculateLoanBasicScenario() {
        LoanRequestDTO request = LoanRequestDTO.builder()
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 6, 30))
                .firstPaymentDate(LocalDate.of(2025, 1, 15))
                .loanAmount(BigDecimal.valueOf(1000))
                .interestRate(BigDecimal.valueOf(12))
                .build();

        doNothing().when(validator).validate(any());

        List<LoanResponseDTO> result = service.calculate(request);

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getOutstandingBalance()).isEqualByComparingTo(BigDecimal.valueOf(1000));
    }

    @Test
    void testCalculateLoanThrowsOnInvalidRequest() {
        LoanRequestDTO request = LoanRequestDTO.builder()
                .startDate(LocalDate.of(2025, 6, 1))
                .endDate(LocalDate.of(2025, 1, 1))
                .firstPaymentDate(LocalDate.of(2025, 6, 15))
                .loanAmount(BigDecimal.valueOf(1000))
                .interestRate(BigDecimal.valueOf(12))
                .build();

        doThrow(new IllegalArgumentException("Data Incial deve ser anterior a Data Final."))
                .when(validator).validate(any());

        assertThatThrownBy(() -> service.calculate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Data Incial deve ser anterior a Data Final");
    }
}
