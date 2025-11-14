package com.test.loan_calculator.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.test.loan_calculator.dtos.LoanRequestDTO;
import com.test.loan_calculator.dtos.LoanResponseDTO;
import com.test.loan_calculator.validation.LoanValidator;

@Service
public class LoanCalculatorService {

    @Autowired
    private LoanValidator validator;

    public LoanCalculatorService(LoanValidator validator) {
        this.validator = validator;
    }

    @Value("${loan.config.days-base}")
    private int DAYS_BASE;

    @Value("${loan.config.scale}")
    private int SCALE;

    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    public List<LoanResponseDTO> calculate(LoanRequestDTO loanRequest) {

        validator.validate(loanRequest);

        TreeSet<LocalDate> allDates = generateAllPaymentAndCompentenceDates(loanRequest);

        int totalInstallments = (int) ChronoUnit.MONTHS.between(loanRequest.getFirstPaymentDate(), loanRequest.getEndDate()) + 1;

        Set<LocalDate> paymentDates = buildPaymentDates(loanRequest, totalInstallments);

        totalInstallments = paymentDates.size();

        List<LoanResponseDTO> result = new ArrayList<>();

        BigDecimal principalBalance = loanRequest.getLoanAmount().setScale( SCALE, ROUNDING );
        BigDecimal accumulatedInterest = BigDecimal.ZERO.setScale(SCALE, ROUNDING);
        BigDecimal loanValue = loanRequest.getLoanAmount().setScale(SCALE, ROUNDING);

        BigDecimal annualRate = loanRequest.getInterestRate().divide(BigDecimal.valueOf(100), 12, ROUNDING);

        int consolidatedCount = 0;
        LocalDate previous = allDates.first();

        result.add(
                LoanResponseDTO.builder()
                        .competenceDate(previous)
                        .loanValue(loanValue)
                        .outstandingBalance(principalBalance)
                        .installmentConsolidated("")
                        .installmentTotal(BigDecimal.ZERO.setScale(SCALE, ROUNDING))
                        .amortization(BigDecimal.ZERO.setScale(SCALE, ROUNDING))
                        .principalBalance(principalBalance.setScale(SCALE, ROUNDING))
                        .interestProvision(BigDecimal.ZERO.setScale(SCALE, ROUNDING))
                        .interestAccumulated(BigDecimal.ZERO.setScale(SCALE, ROUNDING))
                        .interestPaid(BigDecimal.ZERO.setScale(SCALE, ROUNDING))
                        .build()
        );

        allDates.remove(previous);

        for (LocalDate current : allDates) {

            int days = (int) ChronoUnit.DAYS.between(previous, current);
            if (days < 0) days = 0;

            BigDecimal exponent = BigDecimal.valueOf(days).divide(BigDecimal.valueOf(DAYS_BASE), 12, ROUNDING);

            BigDecimal power = BigDecimal.valueOf(Math.pow(annualRate.add(BigDecimal.ONE).doubleValue(), exponent.doubleValue()));

            BigDecimal provision = power.subtract(BigDecimal.ONE)
                    .multiply(principalBalance.add(accumulatedInterest))
                    .setScale(10, RoundingMode.HALF_UP);

            boolean isPayment = paymentDates.contains(current);
            
            boolean isCompetence = isEndOfMonth(current, loanRequest.getStartDate(), loanRequest.getEndDate());

            BigDecimal amortization = BigDecimal.ZERO;
            BigDecimal paidInterest = BigDecimal.ZERO;
            BigDecimal installmentTotal = BigDecimal.ZERO;
            String consolidated = "";

            if (isPayment) {
                consolidatedCount++;

                if (consolidatedCount == totalInstallments) {
                    amortization = principalBalance;
                } else {
                    amortization = loanValue.divide(BigDecimal.valueOf(totalInstallments), 12, ROUNDING);
                }

                paidInterest = accumulatedInterest.add(provision);

                installmentTotal = amortization.add(paidInterest);

                principalBalance = principalBalance.subtract(amortization).setScale(12, ROUNDING);
                accumulatedInterest = accumulatedInterest.add(provision).subtract(paidInterest).setScale(12, ROUNDING);

                consolidated = consolidatedCount + "/" + totalInstallments;
            } else {
                accumulatedInterest = accumulatedInterest.add(provision).setScale(12, ROUNDING);
            }

            BigDecimal outstanding = principalBalance.add(accumulatedInterest).setScale(SCALE, ROUNDING);

            result.add(
                    LoanResponseDTO.builder()
                            .competenceDate(current)
                            .loanValue(isCompetence && !isPayment ? BigDecimal.ZERO.setScale(SCALE, ROUNDING) : (isPayment && result.size()==1 ? loanValue : BigDecimal.ZERO.setScale(SCALE, ROUNDING)))
                            .outstandingBalance(outstanding)
                            .installmentConsolidated(consolidated)
                            .installmentTotal(installmentTotal.setScale(SCALE, ROUNDING))
                            .amortization(amortization.setScale(SCALE, ROUNDING))
                            .principalBalance(principalBalance.setScale(SCALE, ROUNDING))
                            .interestProvision(provision.setScale(SCALE, ROUNDING))
                            .interestAccumulated(accumulatedInterest.setScale(SCALE, ROUNDING))
                            .interestPaid(paidInterest.setScale(SCALE, ROUNDING))
                            .build()
            );

            previous = current;
        }

        return result;
    }

    private Set<LocalDate> buildPaymentDates(LoanRequestDTO loan, int totalInstallments) {
        Set<LocalDate> payments = new HashSet<>();
        LocalDate first = loan.getFirstPaymentDate();
        int originalDay = first.getDayOfMonth();

        for (int i = 0; i < totalInstallments; i++) {
            LocalDate candidate;
            if (i == 0) {
                candidate = first;
            } else {
                LocalDate plus = first.plusMonths(i);
                int lastDay = plus.lengthOfMonth();
                int day = Math.min(originalDay, lastDay);
                candidate = plus.withDayOfMonth(day);
            }

            if (i > 0) {
                if (!isBusinessDay(candidate)) {
                    LocalDate nextUtil = getNextUtilDay(candidate);
                    if (nextUtil.getMonthValue() == candidate.getMonthValue()) {
                        candidate = nextUtil;
                    } else {
                        candidate = lastBusinessDayOfMonth(candidate);
                    }
                }
            }
            if (!candidate.isAfter(loan.getEndDate())) {
                payments.add(candidate);
            }
        }
        
        payments.add(loan.getEndDate());

        return payments;
    }

    private boolean isEndOfMonth(LocalDate date, LocalDate start, LocalDate end) {
        LocalDate eom = YearMonth.from(date).atEndOfMonth();
        return date.equals(eom) && ( !date.isBefore(start) && !date.isAfter(end) );
    }

    private TreeSet<LocalDate> generateAllPaymentAndCompentenceDates(LoanRequestDTO loan) {
        TreeSet<LocalDate> dates = new TreeSet<>();

        dates.add(loan.getStartDate());

        LocalDate currentDate = loan.getStartDate();
        boolean changeCurrentDate = false;

        int originalPaymentDay = loan.getFirstPaymentDate().getDayOfMonth();

        while (!currentDate.isAfter(loan.getEndDate())) {
            LocalDate lastDateOfMonth = currentDate.withDayOfMonth(currentDate.lengthOfMonth());

            if (currentDate.getMonthValue() == loan.getFirstPaymentDate().getMonthValue() &&
                        currentDate.getYear() == loan.getFirstPaymentDate().getYear() &&
                        !changeCurrentDate) {
                currentDate = loan.getFirstPaymentDate();
                changeCurrentDate = true;
            }
            if (currentDate.isAfter(loan.getEndDate())) break;

            if (changeCurrentDate && currentDate.isBefore(lastDateOfMonth)) {
                if (!isBusinessDay(currentDate)) {
                    LocalDate nextUtil = getNextUtilDay(currentDate);
                    if (nextUtil.getMonthValue() == currentDate.getMonthValue()) {
                        dates.add(nextUtil);
                    }
                } else {
                    dates.add(currentDate);
                }
            }
            dates.add(lastDateOfMonth);
            currentDate = addMonthKeepingOriginalDay(currentDate, originalPaymentDay);
        }

        dates.add(loan.getEndDate());

        return dates;
    }

    private LocalDate addMonthKeepingOriginalDay(LocalDate referenceDate, int originalDay) {
        LocalDate nextMonth = referenceDate.plusMonths(1);

        int year = nextMonth.getYear();
        int month = nextMonth.getMonthValue();
        int lastDay = LocalDate.of(year, month, 1).lengthOfMonth();

        int targetDay = Math.min(originalDay, lastDay);

        LocalDate candidate = LocalDate.of(year, month, targetDay);

        if (!isBusinessDay(candidate)) {
            LocalDate nextUtil = getNextUtilDay(candidate);
            if (nextUtil.getMonthValue() != month) {
                candidate = LocalDate.of(year, month, lastDay);
            } else {
                candidate = nextUtil;
            }
        }

        return candidate;
    }

    private LocalDate lastBusinessDayOfMonth(LocalDate date) {
        LocalDate last = date.withDayOfMonth(date.lengthOfMonth());
        while (!isBusinessDay(last)) {
            last = last.minusDays(1);
        }
        return last;
    }

    private LocalDate getNextUtilDay(LocalDate date) {
        LocalDate next = date.plusDays(1);
        while (!isBusinessDay(next)) {
            next = next.plusDays(1);
        }
        return next;
    }

    private boolean isBusinessDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY && !getHolidays(date.getYear()).contains(date);
    }

    private Set<LocalDate> getHolidays(int year) {
        Set<LocalDate> holidays = new HashSet<>();
        holidays.add(LocalDate.of(year, 1, 1));  // Confraternização Universal
        holidays.add(LocalDate.of(year, 4, 21)); // Tiradentes
        holidays.add(LocalDate.of(year, 5, 1));  // Dia do Trabalho
        holidays.add(LocalDate.of(year, 9, 7));  // Independência do Brasil
        holidays.add(LocalDate.of(year, 10, 12)); // Nossa Senhora Aparecida
        holidays.add(LocalDate.of(year, 11, 2)); // Finados
        holidays.add(LocalDate.of(year, 11, 15)); // Proclamação da República
        holidays.add(LocalDate.of(year, 12, 25)); // Natal
        return holidays;
    }
}