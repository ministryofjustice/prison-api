package net.syscon.util;

import java.time.LocalDate;

import static java.time.temporal.ChronoUnit.YEARS;

public class CalcDateRanges {
    private final LocalDate dobDateFrom;
    private final LocalDate dobDateTo;

    public CalcDateRanges(LocalDate dob, LocalDate dobFrom, LocalDate dobTo, int maxYears) {
        if (dob != null) {
            dobDateFrom = dob;
            dobDateTo = dob;
        } else if (dobFrom != null && dobTo == null) {
            dobDateFrom = dobFrom;
            dobDateTo = adjustYears(dobFrom, maxYears);
        } else if (dobFrom == null && dobTo != null) {
            dobDateFrom = adjustYears(dobTo, maxYears * -1);
            dobDateTo = dobTo;
        } else if (dobFrom != null) {
            dobDateFrom = dobFrom;
            if (isGreaterThanYearSpan(dobFrom, dobTo, maxYears)) {
                dobDateTo = adjustYears(dobFrom, maxYears);
            } else {
                dobDateTo = dobTo;
            }
        } else {
            dobDateFrom = null;
            dobDateTo = null;
        }
    }

    public LocalDate getDobDateFrom() {
        return dobDateFrom;
    }

    public LocalDate getDobDateTo() {
        return dobDateTo;
    }

    private boolean isGreaterThanYearSpan(LocalDate fromDate, LocalDate toDate, int maxYearSpan) {
        long years = YEARS.between(fromDate, toDate);
        return years >= maxYearSpan;
    }

    private LocalDate adjustYears(LocalDate fromLocal, int years) {
        return years < 0 ? fromLocal.minusYears(Math.abs(years)) : fromLocal.plusYears(Math.abs(years));
    }

    public boolean hasDobRange() {
        return dobDateFrom != null && dobDateTo != null;
    }
}