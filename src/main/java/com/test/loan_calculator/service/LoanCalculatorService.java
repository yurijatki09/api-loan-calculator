package com.test.loan_calculator.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.test.loan_calculator.dtos.LoanRequestDTO;
import com.test.loan_calculator.dtos.LoanResponseDTO;
import com.test.loan_calculator.validation.LoanValidator;

import lombok.val;

@Service
public class LoanCalculatorService {

    public LoanCalculatorService(LoanValidator validator) {
        this.validator = validator;
    }


    @Autowired
    private LoanValidator validator;

    public List<LoanResponseDTO> calculate(LoanRequestDTO loanRequest) {
        
        validator.validate(loanRequest);

        List<LoanResponseDTO> loans = new ArrayList<>();
        
        TreeSet<LocalDate> paymentDates = generateAllPaymentDates(loanRequest);

        System.out.println("Datas de pagamento geradas: "+paymentDates.toString());
        //ordenando datas
        paymentDates.stream().sorted();

        System.out.println("Datas de pagamento ordenadas: "+paymentDates.toString());

        return new ArrayList<>();
    }

    private TreeSet<LocalDate> generateAllPaymentDates(LoanRequestDTO loan) {
        TreeSet<LocalDate> dates = new TreeSet<>();

        dates.add(loan.getStartDate());
        dates.add(loan.getFirstPaymentDate());

        LocalDate currentDate = loan.getFirstPaymentDate();
        if(loan.getStartDate().getMonth() != loan.getFirstPaymentDate().getMonth() &&
           loan.getStartDate().getYear() != loan.getFirstPaymentDate().getYear()) {
            // grava ultimo dia do mes da data inicial se for diferente do mes do primeiro pagamento
            dates.add(loan.getStartDate().withDayOfMonth(loan.getStartDate().lengthOfMonth()));
        }
        while (!currentDate.isAfter(loan.getEndDate())) {
            LocalDate lastDateOfMonth = currentDate.withDayOfMonth(currentDate.lengthOfMonth());
            
            currentDate = currentDate.plusMonths(1);
            if(currentDate.isAfter(loan.getEndDate())) break;

            dates.add(currentDate);
            if(!lastDateOfMonth.isAfter(loan.getEndDate())) dates.add(lastDateOfMonth);

        }

        return dates;
    }
}
